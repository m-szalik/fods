package org.gruszecm.fods.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.gruszecm.fods.ConnectionCreator;
import org.gruszecm.fods.log.LogLevel;
import org.gruszecm.fods.log.Logger;

public class JdbcConnectionCreator implements ConnectionCreator {
	private Logger logger;
	private int maxWait;
	private int maxActive;
	private int maxIdle;
	private int minIdle;
	private int releaseCounter = 0;
	
	private ConnectionHolder connectionHolders[];
	
	public Connection getConnection(int i) throws SQLException {
		ConnectionHolder ch = connectionHolders[i];
		return ch.getConnection();
	}

	public String getConnectionId(int i) {
		ConnectionHolder ch = connectionHolders[i];
		return ch.getDbId();
	}

	public int getLoginTimeout() throws SQLException {
		return DriverManager.getLoginTimeout();
	}

	public void init(Logger logger, Properties properties) throws Exception {
		this.logger = logger;
		String driver = getProperty(properties, "driverClassName", null);
		Class.forName(driver).newInstance();
		logger.debug("Driver " + driver + " loaded.");
		String username = getProperty(properties, "username", null);
		String password = getProperty(properties, "password", null);
		this.maxActive = Integer.valueOf(getProperty(properties, "maxactive", "8"));
		this.maxIdle = Integer.valueOf(getProperty(properties, "maxidle", "8"));
		this.minIdle = Integer.valueOf(getProperty(properties, "minidle", "0"));
		this.maxWait = Integer.valueOf(getProperty(properties, "maxwait", "-1"));
		if (maxIdle < minIdle) throw new IllegalArgumentException("maxIdle < minIdle");
		DriverManager.setLoginTimeout(maxWait);
		// create ConnectionHolders
		Properties conProperties = (Properties) properties.get("connectionProperties");
		JdbcParameter[] jps = JdbcParameter.parse(getProperty(properties, "url", null));
		ConnectionHolder[] connectionHolders = new ConnectionHolder[jps.length];
		for(int i=0; i<jps.length; i++) {
			JdbcParameter jp = jps[i];
			jp.password = password;
			jp.username = username;
			connectionHolders[i] = new ConnectionHolder(jp, conProperties);
		}
		this.connectionHolders = connectionHolders;
	}
	
	private String getProperty(Properties properties, String key, String defaultValue) {
		String prop = properties.getProperty(key.toLowerCase());
		if (prop == null) {
			if (defaultValue == null) throw new IllegalArgumentException("Missing configurtaion property \"" + key + "\"");
			else prop = defaultValue;
		}
		return prop;		
	}

	public int numOfDatabases() {
		return connectionHolders.length;
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		DriverManager.setLoginTimeout(seconds);
	}
	
	public void markAbandonedConnection(int i, Connection connection) {
		if (connection instanceof JdbcWrappedConnection) {
			JdbcWrappedConnection jdbcwc = (JdbcWrappedConnection) connection;
			ConnectionHolder ch = connectionHolders[i];
			ch.removeConnection(jdbcwc);
		}
	}
	
	private void cleanup() {
		releaseCounter++;
		if (releaseCounter > 40) {
			releaseCounter = 0;
			for(ConnectionHolder ch : connectionHolders) {
				if(ch.connections.size() > maxIdle) {
					ch.doCleanup();
				}
			}
		}
	}

	
	class ConnectionHolder {
		private List<JdbcWrappedConnection> connections;
		private int borrowedCounter = 0;
		private JdbcParameter jdbcParameter;
		private Properties conProperties;
		
		public ConnectionHolder(JdbcParameter jp, Properties conProperties) {
			this.connections = new LinkedList<JdbcWrappedConnection>();
			this.jdbcParameter = jp;
			if (conProperties == null) {
				this.conProperties = new Properties();	
			} else {
				this.conProperties = conProperties;
			}
			this.conProperties.put("user", jp.getUsername());
		    this.conProperties.put("password", jp.getPassword());
		}
		
		public synchronized void doCleanup() {
			int i;
			// close idle
			for(i=maxIdle; i<connections.size(); i++) {
				JdbcWrappedConnection wcon = connections.get(i);
				connections.remove(i);
				try {  wcon.realClose();  } catch (SQLException e) { }
			}
			if (i>0) logger.debug("Closing " + i + " idle connections to " + jdbcParameter.getDbId());
			// create connections if less then minIdle
			try {
				for(i=0; i<(maxIdle - connections.size()); i++) {
					JdbcWrappedConnection wcon = createNewConnection();
					connections.add(wcon);
				}
				logger.debug("Created " + i + " connections to " + jdbcParameter.getDbId());
			} catch (SQLException e) { logger.log(LogLevel.CRITICAL, "Can not create connection to " + jdbcParameter.getDbId(), e);	}
		}
		
		public synchronized void removeConnection(JdbcWrappedConnection con) {
			int ind = connections.indexOf(con);
			if (ind > -1) {
				connections.remove(ind);
			}
			try { con.realClose(); } catch (SQLException e) {	}
		}
			

		public synchronized Connection getConnection() throws SQLException {
			Connection connection = null;
			if (connections.isEmpty()) {
				if (borrowedCounter >= maxActive) {
					throw new SQLException("Can not create more then " + maxActive + " connections to " + jdbcParameter.getDbId() + ".");
				} else {
					connection = createNewConnection();
				}
			} else {
				connection = connections.get(0);
			}
			return connection;
		}

		private JdbcWrappedConnection createNewConnection() throws SQLException {
			Connection c;
			c = DriverManager.getConnection(jdbcParameter.getUrl(), conProperties);
			return new JdbcWrappedConnection(c, this);
		}

		public String getDbId() {
			return jdbcParameter.getDbId();
		}
		
		synchronized void release(JdbcWrappedConnection c) {
			connections.add(c);
			borrowedCounter--;
			cleanup();
		}
	}

}

class JdbcWrappedConnection implements Connection {
	private Connection conn;
	private JdbcConnectionCreator.ConnectionHolder ch;
	
	public JdbcWrappedConnection(Connection c, JdbcConnectionCreator.ConnectionHolder ch) {
		this.conn = c;
		this.ch = ch;
	}
	public void clearWarnings() throws SQLException {
		conn.clearWarnings();
	}
	public void close() throws SQLException {
		ch.release(this);
	}
	public void commit() throws SQLException {
		conn.commit();
	}
	public Statement createStatement() throws SQLException {
		return conn.createStatement();
	}
	public Statement createStatement(int resultSetType,	int resultSetConcurrency, int resultSetHoldability)	throws SQLException {
		return conn.createStatement(resultSetType, resultSetConcurrency,resultSetHoldability);
	}
	public Statement createStatement(int resultSetType, int resultSetConcurrency)throws SQLException {
		return conn.createStatement(resultSetType, resultSetConcurrency);
	}
	public boolean getAutoCommit() throws SQLException {
		return conn.getAutoCommit();
	}
	public String getCatalog() throws SQLException {
		return conn.getCatalog();
	}
	public int getHoldability() throws SQLException {
		return conn.getHoldability();
	}
	public DatabaseMetaData getMetaData() throws SQLException {
		return conn.getMetaData();
	}
	public int getTransactionIsolation() throws SQLException {
		return conn.getTransactionIsolation();
	}
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return conn.getTypeMap();
	}
	public SQLWarning getWarnings() throws SQLException {
		return conn.getWarnings();
	}
	public boolean isClosed() throws SQLException {
		return conn.isClosed();
	}
	public boolean isReadOnly() throws SQLException {
		return conn.isReadOnly();
	}
	public String nativeSQL(String sql) throws SQLException {
		return conn.nativeSQL(sql);
	}
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)	throws SQLException {
		return conn.prepareCall(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}
	public CallableStatement prepareCall(String sql, int resultSetType,	int resultSetConcurrency) throws SQLException {
		return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
	}
	public CallableStatement prepareCall(String sql) throws SQLException {
		return conn.prepareCall(sql);
	}
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return conn.prepareStatement(sql, autoGeneratedKeys);
	}
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return conn.prepareStatement(sql, columnIndexes);
	}
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return conn.prepareStatement(sql, columnNames);
	}
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return conn.prepareStatement(sql);
	}
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		conn.releaseSavepoint(savepoint);
	}
	public void rollback() throws SQLException {
		conn.rollback();
	}
	public void rollback(Savepoint savepoint) throws SQLException {
		conn.rollback(savepoint);
	}
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		conn.setAutoCommit(autoCommit);
	}
	public void setCatalog(String catalog) throws SQLException {
		conn.setCatalog(catalog);
	}
	public void setHoldability(int holdability) throws SQLException {
		conn.setHoldability(holdability);
	}
	public void setReadOnly(boolean readOnly) throws SQLException {
		conn.setReadOnly(readOnly);
	}
	public Savepoint setSavepoint() throws SQLException {
		return conn.setSavepoint();
	}
	public Savepoint setSavepoint(String name) throws SQLException {
		return conn.setSavepoint(name);
	}
	public void setTransactionIsolation(int level) throws SQLException {
		conn.setTransactionIsolation(level);
	}
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		conn.setTypeMap(map);
	}
	public void realClose() throws SQLException {
		conn.close();
	}
}

