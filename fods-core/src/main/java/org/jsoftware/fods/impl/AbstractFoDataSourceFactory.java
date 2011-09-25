package org.jsoftware.fods.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;

import org.jsoftware.fods.client.FodsEventListener;
import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.client.ext.ManageableViaMXBean;
import org.jsoftware.fods.jmx.FoDataSourceConsole;

/**
 * Factory of {@link FODataSource} object.
 * <p>
 * This is the only way to create {@link FODataSource} object.
 * <p>
 * 
 * @author szalik
 */
public abstract class AbstractFoDataSourceFactory {
	public static final String FODS_JMX_SUFIX = "ds";

	public DataSource getObjectInstance() throws IOException {
		Configuration configuration = getConfiguration();
		ObjectName mxbeanObjectName = configuration.getMxBeanObjectName(AbstractFoDataSourceFactory.FODS_JMX_SUFIX);
		FODataSource ds = new FODataSource(configuration);
		if (mxbeanObjectName != null) {
			registerMXBeanForDS(ds, configuration, mxbeanObjectName);
			registerMXBeanFromFactory(configuration.getLogger(), configuration, "logger");
			registerMXBeanFromFactory(configuration.getSelector(), configuration, "selector");
			for (Configuration.DatabaseConfiguration dbc : configuration.getDatabaseConfigurations()) {
				registerMXBeanFromFactory(dbc.getConnectionCreator(), configuration, "database-" + dbc.getDatabaseName());
			}
		}
		displayInfo(configuration);
		return ds;
	}

	protected abstract Configuration getConfiguration() throws IOException;

	private static void displayInfo(Configuration configuration) {
		// display information
		try {
			InputStream ins = FODataSource.class.getResourceAsStream("/org/jsoftware/fods/message.txt");
			if (ins != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(ins));
				StringBuilder out = new StringBuilder();
				String s;
				while ((s = br.readLine()) != null) {
					out.append(s).append('\n');
				}
				s = out.toString();
				Object selector = configuration.getSelector();
				s = s.replaceAll("%databaseSelector%", selector == null ? "-" : selector.getClass().getSimpleName());
				s = s.replace("%dbsCount%", Integer.toString(configuration.getDatabaseConfigurations().length));
				s = s.replace("%fodsName%", configuration.getFoDSName());
				ObjectName on = configuration.getMxBeanObjectName(AbstractFoDataSourceFactory.FODS_JMX_SUFIX);
				s = s.replace("%mxbeanObjectName%", on == null ? "-" : on.toString());
				if (s.length() > 0) {
					System.out.println(s);
				}
				br.close();
			} // if ins
		} catch (IOException e) {
		}
	}

	private void registerMXBeanFromFactory(Object objectToCheck, Configuration configuration, String sufix) {
		Object bean = null;
		if (objectToCheck instanceof ManageableViaMXBean) {
			bean = ((ManageableViaMXBean) objectToCheck).getMXBeanInstance(); 
		}
		if (bean != null) {
			ObjectName objectName = configuration.getMxBeanObjectName(sufix);
			try {
				MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
				if (mbs.isRegistered(objectName)) {
					mbs.unregisterMBean(objectName);
				}
				mbs.registerMBean(bean, objectName);
				configuration.getLogger().info("Main MXBean registered at " + objectName);
			} catch (Exception e) {
				throw new RuntimeException("Error registering MXBean for " + objectToCheck.getClass().getSimpleName() + " in " + configuration.getFoDSName() + " as \""	+ objectName.toString(), e);
			}
		}
	}

	private void registerMXBeanForDS(FODataSource ds, Configuration configuration, ObjectName objectName) {
		String dsName = configuration.getFoDSName();
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			Object bean = new FoDataSourceConsole(ds, configuration);
			if (bean instanceof FodsEventListener) {
				ds.addChangeEventListener((FodsEventListener) bean);
			}
			if (mbs.isRegistered(objectName)) {
				mbs.unregisterMBean(objectName);
			}
			mbs.registerMBean(bean, objectName);
			configuration.getLogger().info("DS MXBean registered at " + objectName);
		} catch (Exception e) {
			throw new RuntimeException("Error registering DS MXBean for " + dsName + " as \"" + objectName.toString(), e);
		}
	}

}