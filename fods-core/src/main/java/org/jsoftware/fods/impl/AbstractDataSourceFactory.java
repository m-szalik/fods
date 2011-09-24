package org.jsoftware.fods.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;

import org.jsoftware.fods.client.ChangeEventListener;
import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.jmx.FODataSourceConsole;



/**
 * Factory of {@link FODataSource} object
 * @author szalik
 */
public abstract class AbstractDataSourceFactory {
	
	public DataSource getObjectInstance() throws IOException {
		Configuration configuration = getConfiguration();
		ObjectName mxbeanObjectName = configuration.getMxBeanObjectName();
		FODataSource ds = new FODataSource(configuration);
		displayInfo(configuration);
		if (mxbeanObjectName != null) {
			registerMXBean(ds, configuration, mxbeanObjectName);
		}
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
				if (s.length() > 0) {
					System.out.println(s);
				}
				br.close();
			} // if ins
		} catch (IOException e) { }
	}

	
	/**
	 * @param ds
	 * @param dsName
	 */
	private void registerMXBean(FODataSource ds, Configuration configuration, ObjectName objectName) {
		String dsName = configuration.getFoDSName();
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
			Object bean = new FODataSourceConsole(ds, configuration);
			if (bean instanceof ChangeEventListener) {
				ds.addChangeEventListener((ChangeEventListener) bean);
			}
			if (mbs.isRegistered(objectName)) {
				mbs.unregisterMBean(objectName);
			}
			mbs.registerMBean(bean, objectName);
		} catch (Exception e) {
			throw new RuntimeException("Error registering mxbean for ds-" + dsName + " as \"" + objectName.toString(), e);
		}
	}
		
}
