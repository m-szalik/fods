package org.jsoftware.fods.client.ext;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.management.ObjectName;
import javax.sql.DataSource;

/**
 * Fods main configuration for single {@link DataSource}.
 * @author szalik
 */
public interface Configuration {

	Selector getSelector();
	
	Logger getLogger();
	
	String getFoDSName();

	DatabaseConfiguration[] getDatabaseConfigurations();

	Collection<String> getDatabaseNames();

	DatabaseConfiguration getDatabaseConfigurationByName(String name) throws NoSuchElementException;

	boolean isEnableStats();

	String getProperty(String key);

	String getProperty(String key, Object defaultValue);

	ObjectName getMxBeanObjectName(String sufix);
	
	int getLogLongSqls();

	
	interface DatabaseConfiguration {
		
		ConnectionCreator getConnectionCreator();

		Properties getConnectionProperties();

		String getDatabaseName();

		String getTestSql();
	}

}
