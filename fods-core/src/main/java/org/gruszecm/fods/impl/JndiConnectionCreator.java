package org.gruszecm.fods.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.gruszecm.fods.ConnectionCreator;
import org.gruszecm.fods.log.LogLevel;
import org.gruszecm.fods.log.Logger;

public class JndiConnectionCreator implements ConnectionCreator {
	private DataSourceWithName[] jdnDataSourceWithNames; 
	                           
	public Connection getConnection(int i) throws SQLException {
		DataSourceWithName ds = jdnDataSourceWithNames[i];
		return ds.getDataSource().getConnection();
	}

	public String getConnectionId(int i) {
		DataSourceWithName ds = jdnDataSourceWithNames[i];
		return ds.getName();
	}

	public int getLoginTimeout() throws SQLException {
		return jdnDataSourceWithNames[0].getDataSource().getLoginTimeout();
	}

	public int numOfDatabases() {
		return jdnDataSourceWithNames.length;
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		for(DataSourceWithName dswn : jdnDataSourceWithNames) {
			dswn.getDataSource().setLoginTimeout(seconds);
		}
	}
	
	public void markAbandonedConnection(int i, Connection connection) {	
	}

	public void init(Logger logger, Properties properties) throws Exception {
		String str = properties.getProperty("jndidatasources");
		if (str == null) {
			throw new IllegalArgumentException("Missing parameter \"jndidatasources\".");
		}
		String[] names = str.split(",");
		jdnDataSourceWithNames = new DataSourceWithName[names.length];
		for(int i=0; i<names.length; i++) {
			DataSource ds = getDS(logger, names[i]);
			jdnDataSourceWithNames[i] = new DataSourceWithName(names[i], ds);
			logger.debug("Datasource \"" + names[i] + "\" creation sucessfull.");
		}		
	}
	
	
	private DataSource getDS(Logger logger, String jndiName) {
		try {
			InitialContext initContext = new InitialContext();
			Context envContext  = (Context) initContext.lookup("java:/comp/env");
			logger.info("Lookup for " + jndiName);
			DataSource dsin = (DataSource) envContext.lookup(jndiName);
			logger.info(jndiName + " found");
			return dsin;
		} catch (NamingException e) {
			logger.log(LogLevel.CRITICAL, "Error building datasources", e);
			throw new RuntimeException("Error building datasources", e);			
		}
	}

}


class DataSourceWithName {
	private DataSource dataSource;
	private String name;
	
	public DataSourceWithName(String name, DataSource dataSource) {
		this.name = name;
		this.dataSource = dataSource;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public String getName() {
		return name;
	}
	
}