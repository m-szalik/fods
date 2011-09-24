package org.jsoftware.fods.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jsoftware.fods.RequiredPropertyMissing;
import org.jsoftware.fods.client.ext.ConnectionCreator;
import org.jsoftware.fods.client.ext.ConnectionCreatorFactory;
import org.jsoftware.fods.client.ext.LogLevel;
import org.jsoftware.fods.client.ext.Logger;

public class JndiDataSourceConnectionCreatorFactory implements ConnectionCreatorFactory {

	public ConnectionCreator getConnectionCreator(String dbName, Logger logger, Properties properties) {
		String jndiName = properties.getProperty("jndiName");
		if (jndiName == null) {
			throw new RequiredPropertyMissing(dbName, "jndiName");
		}
		try {
			InitialContext initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			logger.info("Lookup for " + jndiName);
			DataSource dsin = (DataSource) envContext.lookup(jndiName);
			logger.info(jndiName + " found");
			return new JndiDataSourceConnectionCreator(dsin);
		} catch (NamingException e) {
			logger.log(LogLevel.CRITICAL, "Error building datasources", e);
			throw new RuntimeException("Error building datasources", e);
		}

	}

	class JndiDataSourceConnectionCreator implements ConnectionCreator {
		private DataSource ds;
		
		public JndiDataSourceConnectionCreator(DataSource dsin) {
			this.ds = dsin;
		}
		
		public Connection getConnection() throws SQLException {
			return ds.getConnection();
		}

	}
}
