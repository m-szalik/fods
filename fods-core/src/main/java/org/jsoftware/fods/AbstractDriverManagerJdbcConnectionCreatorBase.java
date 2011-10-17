package org.jsoftware.fods;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.jsoftware.fods.client.ext.ConnectionCreator;
import org.jsoftware.fods.client.ext.Displayable;
import org.jsoftware.fods.client.ext.Logger;
import org.jsoftware.fods.impl.utils.PropertiesUtil;

/**
 * Helper class for {@link DriverManager} based {@link ConnectionCreator}s. 
 * <p>
 * Supported parameters: dbname, driverClassName, jdbcURI, maxWait, setReadOnly
 * </p>
 * @author szalik
 */
public abstract class AbstractDriverManagerJdbcConnectionCreatorBase implements ConnectionCreator, Displayable {
	protected Logger logger;
	protected String dbname, jdbcURI;
	protected Properties connectionProperties;
	protected int maxWait;
	private boolean active, setReadOnly;
	
	
	public AbstractDriverManagerJdbcConnectionCreatorBase(String dbname, Logger logger, Properties properties) {
		Properties cprops = new Properties();
		cprops.putAll(properties);
		for(String key : getNonConnectionProperties()) {
			cprops.remove(key);
		}
		cprops.remove("connectionCreatorFactory");
		cprops.remove("driverClassName");
		cprops.remove("maxWait");
		cprops.remove("jdbcURI");
		this.connectionProperties = cprops;
		this.dbname = dbname;
		this.logger = logger;
		PropertiesUtil pu = new PropertiesUtil(properties, dbname);
		pu.loadDriver("driverClassName");
		this.jdbcURI = pu.getProperty("jdbcURI");
		this.maxWait = Integer.valueOf(pu.getProperty("maxWait", "0"));
		if (this.maxWait > 0) {
			this.maxWait = 0;
		}
		this.setReadOnly = Boolean.getBoolean(pu.getProperty("setReadOnly", "false"));
	}
	
	/**
	 * @return list of properties that should be not passed to jdbc driver.
	 */
	protected abstract String[] getNonConnectionProperties();

	public boolean isConnectorCreatorActive() {
		return active;
	}
	
	public Connection getConnection() throws SQLException {
		if (! active) {
			throw new SQLException("Can not obtain connection from \"" + getConnectionCreatorName() + "\". ConnectionCreator stoped.");
		}
		int maxLoginTimeout = DriverManager.getLoginTimeout();
		DriverManager.setLoginTimeout(maxWait);
		Connection con = null;
		try {
			con = createConnection();
		} finally {
			DriverManager.setLoginTimeout(maxLoginTimeout);
		}
		if (con != null && setReadOnly) {
			con.setReadOnly(true);
		}
		return con;
	}

	public void start() throws Exception {
		active = true;
	}

	public void stop() {
		active = false;
	}

	@Override
	public String asString(boolean addDebugInfo) {
		return getConnectionCreatorName() + (addDebugInfo ? "(jdbcURI=" + jdbcURI + ")" : "");
	}

	protected abstract Connection createConnection() throws SQLException;

	protected abstract String getConnectionCreatorName();

}
