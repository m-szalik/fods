package org.jsoftware.fods.tester.host;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class TesterConfig {
	private String fodsConfig;
	private int runsPerThread;
	private int threads;
	private boolean waitAfterTest;
	private String scenarioClass;
	private int maxFails = 10;

	
	public InputStream getFodsConfigInputStream() throws FileNotFoundException {
		File f = new File(fodsConfig);
		InputStream ins = null;
		if (f.exists()) {
			ins = new FileInputStream(f);
		}
		if (ins == null) {
			ins = getClass().getResourceAsStream(fodsConfig);
		}
		if (ins == null) {
			throw new RuntimeException("Can not load FoDS config form " + fodsConfig);
		}
		return ins;
	}

	public int getRunsPerThread() {
		return runsPerThread;
	}
	
	public int getThreads() {
		return threads;
	}

	public TestScenerio getScenerio() {
		Class<?> cl;
		try {
			cl = Class.forName(scenarioClass);
			Object obj = cl.newInstance();
			return (TestScenerio) obj;
		} catch (Exception e) {
			throw new RuntimeException("Can not load scenario - " + scenarioClass, e);
		}
	}

	public boolean waitAfterTest() {
		return waitAfterTest;
	}

	public int getMaxFails() {
		return maxFails ;
	}

	public void setFodsConfig(String fodsConfig) {
		this.fodsConfig = fodsConfig;
	}

	public void setRunsPerThread(int runsPerThread) {
		this.runsPerThread = runsPerThread;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public void setWaitAfterTest(boolean waitAfterTest) {
		this.waitAfterTest = waitAfterTest;
	}

	public void setScenarioClass(String scenarioClass) {
		this.scenarioClass = scenarioClass;
	}

	public void setMaxFails(int maxFails) {
		this.maxFails = maxFails;
	}
	
}
