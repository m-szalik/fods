package org.jsoftware.fods.impl;

import org.jsoftware.fods.MBeanRegistrationException;
import org.jsoftware.fods.client.FodsEventListener;
import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.client.ext.Displayable;
import org.jsoftware.fods.client.ext.ManageableViaMXBean;
import org.jsoftware.fods.jmx.FoDataSourceConsole;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory of {@link FoDataSourceImpl} object.
 * <p>
 * This is the only way to create {@link FoDataSourceImpl} object.
 * <p>
 * @author szalik
 */
public abstract class AbstractFoDataSourceFactory {
	private static final String PACKAGE_PREFIX = "org.jsoftware.fods.";
	public static final String FODS_JMX_SUFFIX = "ds";



	public DataSource getObjectInstance() throws IOException {
		Configuration configuration = getConfiguration();
		ObjectName mxbeanObjectName = configuration.getMxBeanObjectName(AbstractFoDataSourceFactory.FODS_JMX_SUFFIX);
		FoDataSourceImpl ds = new FoDataSourceImpl(configuration);

		if (mxbeanObjectName != null) {
			registerMXBeanForDS(ds, configuration, mxbeanObjectName);
			registerMXBeanFromFactory(configuration.getLogger(), configuration, "logger");
			registerMXBeanFromFactory(configuration.getSelector(), configuration, "selector");
			registerMXBeanFromFactory(configuration, configuration, "configuration");
			for (Configuration.DatabaseConfiguration dbc : configuration.getDatabaseConfigurations()) {
				registerMXBeanFromFactory(dbc.getConnectionCreator(), configuration, "database-" + dbc.getDatabaseName());
			}
		}

		ds.start();
		Map<String, Boolean> testResults = new HashMap<>();
		for (Configuration.DatabaseConfiguration dbc : configuration.getDatabaseConfigurations()) {
			Boolean b = ds.testDatabase(dbc.getDatabaseName());
			if (b == null) {
				b = Boolean.FALSE;
			}
			testResults.put(dbc.getDatabaseName(), b);
		}
		displayInfo(configuration, testResults);
		return ds;
	}



	protected abstract Configuration getConfiguration() throws IOException;



	private static void displayInfo(Configuration configuration, Map<String, Boolean> testResults) {
		// display information
		String logFile = configuration.getProperty("loggerFile", "").trim();
		boolean debug = configuration.getLogger().isDebugEnabled();
		try {
			InputStream ins = FoDataSourceImpl.class.getResourceAsStream("/org/jsoftware/fods/message.txt");
			if (ins != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(ins));
				StringBuilder out = new StringBuilder();
				String s;
				while ((s = br.readLine()) != null) {
					out.append(s).append('\n');
				}
				s = out.toString();
				Object selector = configuration.getSelector();
				s = s.replaceAll("%databaseSelector%", componentToString(selector, debug));
				s = s.replace("%dbsCount%", Integer.toString(configuration.getDatabaseConfigurations().length));
				s = s.replace("%fodsName%", configuration.getFoDSName());
				s = s.replace("%loggerFile%", logFile.length() == 0 ? "-" : logFile);
				ObjectName on = configuration.getMxBeanObjectName(AbstractFoDataSourceFactory.FODS_JMX_SUFFIX);
				s = s.replace("%mxbeanObjectName%", on == null ? "-" : on.toString());
				StringBuilder sb = new StringBuilder(s).append("  FoDS state:");
				for (Map.Entry<String, Boolean> me : testResults.entrySet()) {
					sb.append("\n    Database ").append(me.getKey());
					sb.append(" status ").append(me.getValue() ? "OK" : "FAIL");
					if (debug) {
						sb.append(", connectionCreator: ").append(componentToString(configuration.getDatabaseConfigurationByName(me.getKey()).getConnectionCreator(), debug));
					}
				}
				if (sb.length() > 0) {
					System.out.println(sb.toString() + "\n");
				}
				br.close();
			} // if ins
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	private static String componentToString(Object comp, boolean debug) {
		String str;
		if (comp == null) {
			str = "-";
		} else {
			str = comp.getClass().getName();
			if (str.startsWith(PACKAGE_PREFIX)) {
				str = comp.getClass().getSimpleName();
				if (str == null || str.length() == 0) {
					int i = str.indexOf('$');
					if (i > 0) str = str.substring(0, i);
					if (str.endsWith("Factory")) str = "product of " + str;
				}
			}
		}
		if (comp instanceof Displayable) {
			str = ((Displayable) comp).asString(debug);
		}
		return str;
	}



	private void registerMXBeanFromFactory(Object objectToCheck, Configuration configuration, String suffix) {
		Object bean = null;
		if (objectToCheck instanceof ManageableViaMXBean) {
			bean = ((ManageableViaMXBean) objectToCheck).getMXBeanInstance();
		}
		if (bean != null) {
			ObjectName objectName = configuration.getMxBeanObjectName(suffix);
			try {
				MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
				if (mbs.isRegistered(objectName)) {
					mbs.unregisterMBean(objectName);
				}
				mbs.registerMBean(bean, objectName);
				configuration.getLogger().info("Main MXBean registered at " + objectName);
			} catch (Exception e) {
				throw new MBeanRegistrationException("Error registering MXBean for " + objectToCheck.getClass().getSimpleName() + " in " + configuration.getFoDSName() + " as \"" + objectName.toString(), e);
			}
		}
	}



	private void registerMXBeanForDS(FoDataSourceImpl ds, Configuration configuration, ObjectName objectName) {
		String dsName = configuration.getFoDSName();
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			Object bean = new FoDataSourceConsole(ds, configuration);
			ds.addChangeEventListener((FodsEventListener) bean);
			if (mbs.isRegistered(objectName)) {
				mbs.unregisterMBean(objectName);
			}
			mbs.registerMBean(bean, objectName);
			configuration.getLogger().info("DS MXBean registered at " + objectName);
		} catch (Exception e) {
			throw new MBeanRegistrationException("Error registering DS MXBean for " + dsName + " as \"" + objectName.toString(), e);
		}
	}

}
