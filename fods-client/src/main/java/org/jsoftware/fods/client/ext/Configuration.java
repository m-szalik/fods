package org.jsoftware.fods.client.ext;

import java.util.Collection;
import java.util.Properties;

import javax.management.ObjectName;

/**
 * Fods main configuration for single {@link FODataSource}
 * 
 * @author szalik
 */
public interface Configuration {

	Selector getSelector();

	Logger getLogger();

	String getFoDSName();

	DatabaseConfiguration[] getDatabaseConfigurations();

	Collection<String> getDatabaseNames();

	DatabaseConfiguration getDatabaseConfigurationByName(String name);

	boolean isEnableStats();

	String getProperty(String key);

	String getProperty(String key, Object defaultValue);

	ObjectName getMxBeanObjectName();
	
	interface DatabaseConfiguration {

		ConnectionCreator getConnectionCreator();

		Properties getConnectionProperties();

		String getDatabaseName();

		String getTestSql();
	}

}
