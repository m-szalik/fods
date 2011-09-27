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
import org.jsoftware.fods.client.ext.Logger;
import org.jsoftware.fods.impl.utils.PropertiesUtil;

/**
 * Creates {@link ConnectionCreator} that uses {@link DataSource} from jndi to acquire database {@link Connection}.
 * <p>
 * Required configuration values:
 * <ul>
 * 	<li>jndiName - jndi name (in scope of &quote;java:/comp/env/&quote;) name of {@link DataSource} to use</li>
 * </ul>
 * Optional configuration values:
 * <ul>
 * 	<li>lazyTries</li>
 * </ul>
 * </p>
 * @author szalik
 * @deprecated do not use because of jndi binding order. Target {@link DataSource} may be not available for {@link JndiDataSourceConnectionCreator}.
 */
@Deprecated
public class JndiDataSourceConnectionCreatorFactory implements ConnectionCreatorFactory {
	private String jndiName;
	private String dbName;
	
	public ConnectionCreator getConnectionCreator(String dbName, Logger logger, Properties properties) {
		PropertiesUtil pu = new PropertiesUtil(properties, dbName);
		this.jndiName = pu.getProperty("jndiName");
		this.dbName = dbName;
		int lazy = Integer.valueOf(pu.getProperty("lazyTries", "2"));
		return new JndiDataSourceConnectionCreator(logger, lazy);
	}
	

	class JndiDataSourceConnectionCreator implements ConnectionCreator, Displayable {
		private static final String JAVA_COMP_ENV = "java:/comp/env";
		private Logger logger;
		private DataSource ds;
		private int lazy;
		
		public JndiDataSourceConnectionCreator(Logger logger, int lazy) {
			this.logger = logger;
			this.lazy = lazy;
		}
		
		public Connection getConnection() throws SQLException {
			if (ds == null) {
				if (lazy > 0) {
					lazy--;
					if (! loolup()) {
						logger.debug("Can not find jndi object \"" + fullJndiName() + "\" for database \"" + dbName + "\". Tries left: " + lazy);
					}
				} 
				if (ds == null) {
					throw new SQLException("JndiName \"" + fullJndiName() + " not found for database \"" + dbName + "\". " + (lazy == 0 ? "ConnectionCreator is inactive." : "Please wait."));
				}
			}
			return ds.getConnection();
		}

		public String asString(boolean addDebugInfo) {
			return "JndiDataSourceConnectionCreator" + (addDebugInfo ? "(jndiName=" + jndiName + ")": "");
		}

		public void start() throws Exception {
			loolup();
		}

		public void stop() {
		}
		
		
		private String fullJndiName() {
			return JAVA_COMP_ENV + "/" + jndiName;
		}
		
		private synchronized boolean loolup() {
			try {
				InitialContext initContext = new InitialContext();
				Context envContext = (Context) initContext.lookup(JAVA_COMP_ENV);
				logger.debug("Lookup for \"" + jndiName + "\" for databse \"" + dbName + "\".");
				DataSource dataSource = (DataSource) envContext.lookup(jndiName);
				logger.debug("Jndi object \"" + fullJndiName() + "\" found.");
				ds = dataSource;
				return true;
			} catch (NamingException e) {
				logger.debug("Jndi object \"" + fullJndiName() + "\" not found.");
				return false;
			}
		}
	}
}
