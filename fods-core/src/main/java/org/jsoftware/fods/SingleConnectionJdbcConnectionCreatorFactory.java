package org.jsoftware.fods;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.jsoftware.fods.client.ext.ConnectionCreator;
import org.jsoftware.fods.client.ext.ConnectionCreatorFactory;
import org.jsoftware.fods.client.ext.Displayable;
import org.jsoftware.fods.client.ext.Logger;
import org.jsoftware.fods.impl.utils.PropertiesUtil;

/**
 * Creates {@link ConnectionCreator} that uses pool of {@link Connection}s.
 * <p>
 * Required configuration values:
 * <ul>
 * <li>jdbcURI - database's jdbc uri</li>
 * <li>driverClassName - jdbc {@link Driver}</li>
 * <li><i>other jdbc connection properties</i></li>
 * </ul>
 * Optional configuration values:
 * <ul>
 * <li>maxWait - login timeout</li>
 * </ul>
 * </p>
 * 
 * @author szalik
 */
public class SingleConnectionJdbcConnectionCreatorFactory implements ConnectionCreatorFactory {

	public ConnectionCreator getConnectionCreator(final String dbname, final Logger logger, final Properties properties) {
		PropertiesUtil pu = new PropertiesUtil(properties, dbname);
		final String url = pu.getProperty("jdbcURI");
		pu.loadDriver("driverClassName");

		SingleConnectionJdbcConnection cc = new SingleConnectionJdbcConnection();
		cc.jdbcURI = url;
		cc.dbName = dbname;
		cc.properties = new Properties(properties);
		cc.logger = logger;
		cc.maxWait = Integer.valueOf(pu.getProperty("maxWait", "0"));
		return cc;
	}

	/**
	 * @author szalik
	 */
	class SingleConnectionJdbcConnection implements ConnectionCreator, Displayable {
		private String dbName, jdbcURI;
		private Properties properties;
		private Logger logger;
		private int maxWait;

		public String asString(boolean addDebugInfo) {
			return "SingleConnectionJdbcConnection" + (addDebugInfo ? "(jdbcURI=" + jdbcURI + ")" : "");
		}

		public Connection getConnection() throws SQLException {
			int timoeut = DriverManager.getLoginTimeout();
			DriverManager.setLoginTimeout(maxWait);
			try {
				Connection con = DriverManager.getConnection(jdbcURI, properties);
				logger.debug("New connection to \"" + dbName + "\" created.");
				return con;
			} finally {
				DriverManager.setLoginTimeout(timoeut);
			}
		}

		public void start() throws Exception {
		}

		public void stop() {
		}
		
	}
}
