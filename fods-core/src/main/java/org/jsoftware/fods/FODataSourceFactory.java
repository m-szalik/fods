package org.jsoftware.fods;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.jsoftware.fods.client.ChangeEventListener;
import org.jsoftware.fods.client.ext.Selector;
import org.jsoftware.fods.impl.DefaultSelector;
import org.jsoftware.fods.impl.JdbcConnectionCreator;
import org.jsoftware.fods.impl.JndiConnectionCreator;
import org.jsoftware.fods.jmx.FODataSourceConsole;
import org.jsoftware.fods.log.Logger;
import org.jsoftware.fods.log.PrintWriterLogEventListener;



/**
 * Factory of {@link FODataSource} object
 * @author szalik
 */
public class FODataSourceFactory implements ObjectFactory {

	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
		if (obj instanceof Reference) {
			PrintWriterLogEventListener logEventListener = new PrintWriterLogEventListener(new PrintWriter(System.out));
			Configuration configuration = new Configuration();
			boolean loggerDebug = false;
			boolean jmxOn = true;
			Reference r = (Reference) obj;
			ConnectionCreator connectionCreator = null;
			Properties properties = new Properties();
			for(Enumeration<RefAddr> en=r.getAll(); en.hasMoreElements();) {
				RefAddr ra = en.nextElement();
				if ("testSQL".equalsIgnoreCase(ra.getType())) {
					configuration.setTestSql(ra.getContent().toString());
				}
				if ("backTime".equalsIgnoreCase(ra.getType())) {
					int value = Integer.valueOf(ra.getContent().toString());
					configuration.setBackTime(value);
				}
				if ("logFile".equalsIgnoreCase(ra.getType())) {
					String filename = ra.getContent().toString().trim();
					logEventListener = new PrintWriterLogEventListener(new PrintWriter(new FileWriter(filename)));
				}
				if ("jndiDataSources".equalsIgnoreCase(ra.getType())) {					
					connectionCreator = new JndiConnectionCreator();
				}
				if ("url".equalsIgnoreCase(ra.getType())) {					
					connectionCreator = new JdbcConnectionCreator();
				}
				if ("debug".equalsIgnoreCase(ra.getType())) {
					loggerDebug = Boolean.valueOf(ra.getContent().toString()).booleanValue();
				}
				if ("jmx".equalsIgnoreCase(ra.getType())) {
					jmxOn = Boolean.valueOf(ra.getContent().toString()).booleanValue();
					configuration.setJMX(jmxOn);
				}
				if ("stats".equalsIgnoreCase(ra.getType())) {
					configuration.setEnableStats(Boolean.valueOf(ra.getContent().toString()).booleanValue());
				}
				if ("connectionProperties".equalsIgnoreCase(ra.getType())) {
					Properties conProperties = new Properties();
					for(String ps : ra.getContent().toString().split(";")) {
						String[] cprop = ps.split("=");
						conProperties.put(cprop[0], cprop[1]);
					}
					if (! conProperties.isEmpty()) {
						properties.put("connectionProperties", conProperties);
					}
				}
				
				properties.put(ra.getType().toLowerCase(), ra.getContent().toString());
			} // for
			logEventListener.setDebug(loggerDebug);
			Logger logger = new Logger();
			logger.addLogEventListener(logEventListener);
			Selector selector = new DefaultSelector();
			if (connectionCreator == null) {
				throw new IllegalArgumentException("Can not determinate database access type. Check configuration file.");
			}
			logger.info("Database access is " + connectionCreator.getClass().getSimpleName());
			connectionCreator.init(logger, properties);
			FODataSource ds = new FODataSource(selector, logger, connectionCreator, configuration);
			if (jmxOn) {
				registerMXBean(ds, name.get(0));
			}
			logger.info("DataSource " + name.get(0) + " created.");
			return ds;
		} 
		throw new RuntimeException(obj + " is not " + Reference.class.getName());
	}

	
	/**
	 * @param ds
	 * @param dsName
	 */
	private void registerMXBean(FODataSource ds, String dsName) {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
			ObjectName beanName = new ObjectName(getClass().getPackage().getName() + ":type=ds-" + dsName);
			Object bean = new FODataSourceConsole(ds);
			if (bean instanceof ChangeEventListener) {
				ds.addChangeEventListener((ChangeEventListener) bean);
			}
			if (mbs.isRegistered(beanName)) {
				mbs.unregisterMBean(beanName);
			}
			mbs.registerMBean(bean, beanName);
		} catch (Exception e) {
			throw new RuntimeException("Error registering mxbean for ds-" + dsName, e);
		}
	}
		
}
