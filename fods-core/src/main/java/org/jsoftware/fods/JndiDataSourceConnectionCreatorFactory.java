package org.jsoftware.fods;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.jsoftware.fods.client.ext.ConnectionCreator;
import org.jsoftware.fods.client.ext.ConnectionCreatorFactory;
import org.jsoftware.fods.client.ext.Displayable;
import org.jsoftware.fods.client.ext.LogLevel;
import org.jsoftware.fods.client.ext.Logger;
import org.jsoftware.fods.impl.utils.PropertiesUtil;

/**
 * Creates {@link ConnectionCreator} that uses {@link DataSource} from jndi to acquire database {@link Connection}.
 * <p>
 * Required configuration values:
 * <ul>
 * 	<li>jndiName - jndi name (in scope of &quote;java:/comp/env/&quote;) name of {@link DataSource} to use</li>
 * </ul>
 * </p>
 * @author szalik
 */
public class JndiDataSourceConnectionCreatorFactory implements ConnectionCreatorFactory {
	private String jndiName;
	
	public ConnectionCreator getConnectionCreator(String dbName, Logger logger, Properties properties) {
		PropertiesUtil pu = new PropertiesUtil(properties, dbName);
		jndiName = pu.getProperty("jndiName");
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

	class JndiDataSourceConnectionCreator implements ConnectionCreator, Displayable {
		private DataSource ds;
		
		public JndiDataSourceConnectionCreator(DataSource dsin) {
			this.ds = dsin;
		}
		
		public Connection getConnection() throws SQLException {
			return ds.getConnection();
		}

		public String asString(boolean addDebugInfo) {
			return "JndiDataSourceConnectionCreator" + (addDebugInfo ? "(jndiName=" + jndiName + ")": "");
		}
	}
}
