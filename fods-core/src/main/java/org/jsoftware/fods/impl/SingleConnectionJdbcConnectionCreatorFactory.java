package org.jsoftware.fods.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.jsoftware.fods.RequiredPropertyMissing;
import org.jsoftware.fods.client.ext.ConnectionCreator;
import org.jsoftware.fods.client.ext.ConnectionCreatorFactory;
import org.jsoftware.fods.client.ext.Logger;



public class SingleConnectionJdbcConnectionCreatorFactory implements ConnectionCreatorFactory {

	public ConnectionCreator getConnectionCreator(final String dbname, final Logger logger, final Properties properties) {
		final String url = properties.getProperty("jdbcURI");
		if (url == null) {
			throw new RequiredPropertyMissing(dbname, "jdbcURI");
		}
		return new ConnectionCreator() {
			public Connection getConnection() throws SQLException {
				Connection con = DriverManager.getConnection(url, properties);
				logger.debug("New connection to \"" + dbname + "\" created.");
				return con;
			}
		};
	}
}

