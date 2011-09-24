package org.jsoftware.fods.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.client.ext.Configuration.DatabaseConfiguration;
import org.jsoftware.fods.client.ext.ConfigurationFactory;
import org.jsoftware.fods.client.ext.ConnectionCreator;
import org.jsoftware.fods.client.ext.ConnectionCreatorFactory;
import org.jsoftware.fods.client.ext.LoggerFactory;
import org.jsoftware.fods.client.ext.SelectorFactory;

public class PropertiesBasedConfigurationFactory implements ConfigurationFactory {
	private Properties properties;
	
	public PropertiesBasedConfigurationFactory() {
		properties = new Properties();
	}
	
	public void setProperties(Properties properties) {
		try {
			this.properties = new Properties();
			this.properties.load(getClass().getResourceAsStream("/defaultConfigurationBase.properties"));
		} catch (IOException e) {
			throw new RuntimeException("Can not load defaults.", e);
		}
		for(String k : properties.stringPropertyNames()) {
			this.properties.setProperty(k, properties.getProperty(k));
		}
	}
	
	public Configuration getConfiguration() {
		Properties main = new Properties();
		Map<String,Properties> map = new HashMap<String, Properties>();
		for(Object k : properties.keySet()) {
			String key = k.toString();
			if (key.contains(".")) {
				String[] va  = key.split("\\.", 2);
				Properties p = map.get(va[0]);
				if (p == null) {
					p = new Properties();
				}
				p.put(va[1], properties.getProperty(key));
				map.put(va[0], p);
			} else {
				main.put(key, properties.getProperty(key));
			}
		}
		
		DefaultConfiguration configuration = new DefaultConfiguration(main);
		
		String loggerFactoryClass = main.getProperty("loggerFactory", DefaultLoggerFactory.class.getName());
		try {
			LoggerFactory lf = (LoggerFactory) Class.forName(loggerFactoryClass).newInstance();
			configuration.setLogger(lf.getLogger(main));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	
		DatabaseConfiguration[] dbsco = new DatabaseConfiguration[map.size()];
		int i = 0;
		for(String dbname : map.keySet()) {
			DatabaseConfigurationImpl dbc = new DatabaseConfigurationImpl(dbname);
			dbc.props = map.get(dbname);
			dbc.testSQL = dbc.props.getProperty("testSQL", main.getProperty("testSQL"));
			String fc = dbc.props.getProperty("connectionCreatorFactory");
			try {
				ConnectionCreatorFactory fac = (ConnectionCreatorFactory) Class.forName(fc).newInstance();
				dbc.connectionCreator = fac.getConnectionCreator(dbname, configuration.getLogger(), dbc.props);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			dbsco[i++] = dbc;
		}
		configuration.setDatabases(dbsco);
		
		String selectorFactoryClass = main.getProperty("selectorFactory", DefaultSelectorFactory.class.getName());
		try {
			SelectorFactory sf = (SelectorFactory) Class.forName(selectorFactoryClass).newInstance();
			configuration.setSelector(sf.getSelector(configuration));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return configuration;
	}	
	
	 
}


class DatabaseConfigurationImpl implements DatabaseConfiguration {
	ConnectionCreator connectionCreator;
	Properties props;
	String testSQL;
	private String name;
	
	public DatabaseConfigurationImpl(String name) {
		this.name = name;
	}

	public ConnectionCreator getConnectionCreator() {
		return connectionCreator;
	}

	public Properties getConnectionProperties() {
		return props;
	}

	public String getDatabaseName() {
		return name;
	}

	public String getTestSql() {
		return testSQL;
	}
	
}