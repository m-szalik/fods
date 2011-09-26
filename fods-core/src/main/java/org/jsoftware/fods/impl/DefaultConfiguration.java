package org.jsoftware.fods.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import javax.management.ObjectName;

import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.client.ext.LogLevel;
import org.jsoftware.fods.client.ext.Logger;
import org.jsoftware.fods.client.ext.ManageableViaMXBean;
import org.jsoftware.fods.client.ext.Selector;
import org.jsoftware.fods.impl.utils.PropertiesUtil;

/**
 * Default implementation of {@link FoDataSourceImpl}'s {@link Configuration}.
 * @author szalik
 */
public class DefaultConfiguration implements Configuration, ManageableViaMXBean {
	private Selector selector;
	private Logger logger;
	private String fodsName;
	private DatabaseConfiguration[] databases;
	private PropertiesUtil pu;

	public DefaultConfiguration(Properties main) {
		pu = new PropertiesUtil(main);
		fodsName = getProperty("fodsName");
	}
	
	public Object getMXBeanInstance() {
		// TODO todo
		return null;
	}

	public Selector getSelector() {
		return selector;
	}
	
	public void setSelector(Selector selector) {
		this.selector = selector;
	}
		
	public void setLogger(Logger logger) {
		this.logger = logger;
	}


	public Logger getLogger() {
		return logger;
	}

	public String getFoDSName() {
		return fodsName;
	}

	public Collection<String> getDatabaseNames() {
		Set<String> names = new HashSet<String>();
		for (DatabaseConfiguration dbc : getDatabaseConfigurations()) {
			names.add(dbc.getDatabaseName());
		}
		return Collections.unmodifiableSet(names);
	}

	public DatabaseConfiguration getDatabaseConfigurationByName(String name) throws NoSuchElementException {
		for (DatabaseConfiguration dbc : getDatabaseConfigurations()) {
			if (dbc.getDatabaseName().equals(name)) {
				return dbc;
			}
		}
		throw new NoSuchElementException("No database named \"" + name + "\"");
	}

	public DatabaseConfiguration[] getDatabaseConfigurations() {
		return databases;
	}

	public void setDatabases(DatabaseConfiguration[] databases) {
		this.databases = databases;
	}

	public String getProperty(String key) {
		return pu.getProperty(key);
	}

	public String getProperty(String key, Object defaultValue) {
		return pu.getProperty(key, defaultValue != null ? defaultValue.toString() : null);
	}


	public boolean isEnableStats() {
		return Boolean.valueOf(getProperty("statsEnabled", "false"));
	}

	public ObjectName getMxBeanObjectName(String sufix) {
		if (Boolean.valueOf(getProperty("registerMxBean", "false"))) {
			String str = "org.jsoftware.fods." + getFoDSName() + ":type=" + sufix;
			try {
				return new ObjectName(str);
			} catch (Exception e) {
				logger.log(LogLevel.WARN, "Invalid MxBean ObjectName - \"" + str + "\".", e);
			}
		}
		return null;
	}

}
