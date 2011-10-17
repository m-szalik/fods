package org.jsoftware.fods;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.jsoftware.fods.client.ext.ConnectionCreator;
import org.jsoftware.fods.client.ext.ConnectionCreatorFactory;
import org.jsoftware.fods.client.ext.Logger;

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
 * <li>setReadOnly=true - set connection as read only</li>
 * </ul>
 * </p>
 * 
 * @author szalik
 */
public class SingleConnectionJdbcConnectionCreatorFactory implements ConnectionCreatorFactory {

	public ConnectionCreator getConnectionCreator(final String dbname, final Logger logger, final Properties properties) {
		return new SingleConnectionJdbcConnectionCreator(dbname, logger, properties);
	}

	/**
	 * @author szalik
	 */
	class SingleConnectionJdbcConnectionCreator extends AbstractDriverManagerJdbcConnectionCreatorBase {
		public SingleConnectionJdbcConnectionCreator(String dbname, Logger logger, Properties properties) {
			super(dbname, logger, properties);
		}

		protected String getConnectionCreatorName() {
			return "SingleConnectionJdbcConnection";
		}
	
		protected Connection createConnection() throws SQLException {
			Connection con = DriverManager.getConnection(jdbcURI, connectionProperties);
			logger.debug("New connection to \"" + dbname + "\" created.");
			return con;
		}
		
		@Override
		protected String[] getNonConnectionProperties() {
			return new String[0];
		}
		
	}
}
