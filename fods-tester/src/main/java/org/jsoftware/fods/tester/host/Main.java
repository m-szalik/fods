package org.jsoftware.fods.tester.host;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.jsoftware.fods.FoDataSource;

public class Main {
	private TesterConfig testerConfig;
	private DataSource fods;
	private boolean shouldBreak;

	public static void main(String[] args) throws Exception {
		Main main = new Main();
		String file = System.getProperty("config");
		if (file != null) {
			main.readConfig(new FileInputStream(new File(file)));
		} else {
			main.readConfig(Main.class.getResourceAsStream("config.properties"));
		}
		main.fods = main.createFods();
		long ts = System.currentTimeMillis();
		try {
			main.runTest();
		} finally {
			System.out.println("Test run " + ((System.currentTimeMillis() - ts) / 1000) + "s.");
			waitForInput("Press ENTER");
		}
	}

	private static String waitForInput(String msg) {
		System.out.print(msg + " ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			return br.readLine();
		} catch (IOException e) {
			return null;
		}
	}

	private DataSource createFods() throws IOException {
		Properties fodsProperties = new Properties();
		fodsProperties.load(testerConfig.getFodsConfigInputStream());
		return new FoDataSource(fodsProperties);
	}

	private void runTest() throws Exception {
		final ThreadGroup tg = new ThreadGroup("testers");
		final int tcount = testerConfig.getRunsPerThread();
		final TestScenerio scenerio = testerConfig.getScenerio();
		scenerio.init(testerConfig, fods);
		scenerio.before();
		final AtomicInteger runners = new AtomicInteger(testerConfig.getThreads());
		Runnable run = new Runnable() {
			@Override
			public void run() {
				int fails = testerConfig.getMaxFails();
				for (int i = 0; i < tcount; i++) {
					try {
						scenerio.test();
					} catch (SQLException e) {
						fails--;
						System.out.println("*** " + Thread.currentThread().getName() + " test exception - " + e + " fails to stop " + fails);
						e.printStackTrace();
						if (fails == 0) break;
					}
					if (shouldBreak) break;
				}
				runners.decrementAndGet();
				if (testerConfig.waitAfterTest()) {
					while (!shouldBreak) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							break;
						}
					}
				}
			}
		};
		for (int i = 0; i < testerConfig.getThreads(); i++) {
			Thread th = new Thread(tg, run);
			th.setName("tester-" + i);
			th.setDaemon(true);
			th.start();
		}
		System.out.println("Working...");
		try {
			do {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					System.out.println("Test stopped.");
					shouldBreak = true;
					break;
				}
				System.out.print(runners.get() + " ");
			} while (runners.get() > 0);
		} finally {
			scenerio.after();
			System.out.println("\nTest run for " + scenerio.getClass().getSimpleName() + " complete.");
		}
		
	}

	private void readConfig(InputStream configInputStream) throws IOException, IllegalAccessException, InvocationTargetException {
		Properties properties = new Properties();
		properties.load(configInputStream);
		TesterConfig tc = new TesterConfig();
		BeanUtils.populate(tc, properties);
		testerConfig = tc;
	}

}
