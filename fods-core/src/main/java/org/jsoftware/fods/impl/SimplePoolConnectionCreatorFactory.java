package org.jsoftware.fods.impl;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jsoftware.fods.RequiredPropertyMissing;
import org.jsoftware.fods.client.ext.ConnectionCreator;
import org.jsoftware.fods.client.ext.ConnectionCreatorFactory;
import org.jsoftware.fods.client.ext.Logger;

public class SimplePoolConnectionCreatorFactory implements ConnectionCreatorFactory {

	public ConnectionCreator getConnectionCreator(String dbName, Logger logger, Properties properties) {
		try {
			SimplePoolConnectionCreator creator = new SimplePoolConnectionCreator(dbName, logger, properties);
			return creator;
		} catch (Exception e) {
			throw new RuntimeException("Can not create " + SimplePoolConnectionCreator.class.getName(), e);
		}
	}

}

class SimplePoolConnectionCreator extends Thread implements ConnectionCreator {
	private Logger logger;
	private int maxWait;
	private int maxActive;
	private int maxIdle;
	private int minIdle;
	private long closeTimout;
	private int usageCounter = 0;
	private String dbname, jdbcURI;
	private Properties connectionProperties;
	private List<JdbcWrappedConnection> activeConnections;
	private List<JdbcWrappedConnection> availableConnections;

	public SimplePoolConnectionCreator(String dbname, Logger logger, Properties properties) throws Exception {
		super(SimplePoolConnectionCreator.class.getSimpleName() + "-cleanupThread");
		this.logger = logger;
		this.dbname = dbname;
		this.connectionProperties = properties;
		String driver = getProperty(properties, "driverClassName", null);
		Class.forName(driver).newInstance();
		logger.debug("Driver " + driver + " loaded.");
		this.maxActive = Integer.valueOf(getProperty(properties, "maxActive", "8"));
		this.maxIdle = Integer.valueOf(getProperty(properties, "maxIdle", "1"));
		this.minIdle = Integer.valueOf(getProperty(properties, "minIdle", "0"));
		this.maxWait = Integer.valueOf(getProperty(properties, "maxWait", "-1"));
		if (maxIdle < minIdle) {
			throw new IllegalArgumentException("maxIdle < minIdle");
		}
		this.jdbcURI = getProperty(properties, "jdbcURI", null);
		this.closeTimout = Integer.valueOf(getProperty(properties, "closeTimeout", "3600000"));
		if (maxWait >= 0) {
			DriverManager.setLoginTimeout(maxWait);
		}
		this.activeConnections = new LinkedList<SimplePoolConnectionCreator.JdbcWrappedConnection>();
		this.availableConnections = new LinkedList<SimplePoolConnectionCreator.JdbcWrappedConnection>();
		for (int a = 0; a < this.minIdle; a++) {
			this.availableConnections.add(createNewConnection());
		}
		setDaemon(true);
		start();
	}

	private JdbcWrappedConnection createNewConnection() throws SQLException {
		Connection con = DriverManager.getConnection(jdbcURI, connectionProperties);
		JdbcWrappedConnection jdbcWrappedConnection = new JdbcWrappedConnection(con);
		synchronized (activeConnections) {
			if (activeConnections.size() == maxActive) {
				con.close();
				throw new SQLException("Can not create more connections to " + dbname + ". MaxActive is set to " + maxActive);
			}
			activeConnections.add(jdbcWrappedConnection);
		}
		logger.debug("New connection to \"" + dbname + "\" created for pool.");
		return jdbcWrappedConnection;
	}

	public Connection getConnection() throws SQLException {
		JdbcWrappedConnection con = null;
		synchronized (availableConnections) {
			if (!availableConnections.isEmpty()) {
				con = availableConnections.remove(0);
			}
		}
		if (con != null && con.isClosed()) {
			abandon(con);
			con = createNewConnection();
		}
		if (con == null) {
			con = createNewConnection();
		}
		usageCounter++;
		con.ts = System.currentTimeMillis();
		return con;
	}

	private void abandon(JdbcWrappedConnection con) {
		synchronized (activeConnections) {
			activeConnections.remove(con);
		}
		synchronized (availableConnections) {
			availableConnections.remove(con);
		}
		logger.debug("Connection to \"" + dbname + "\" abandoned.");
	}

	void release(JdbcWrappedConnection conn) {
		conn.ts = -1;
		synchronized (availableConnections) {
			availableConnections.add(conn);
		}
	}

	@Override
	public void run() {
		try {
			while (true) {
				Thread.sleep(closeTimout);
				long ts = System.currentTimeMillis() - closeTimout;
				synchronized (activeConnections) {
					for (JdbcWrappedConnection jc : activeConnections) {
						if (jc.ts < ts) {
							logger.warn("Connection " + jc + " not released in " + closeTimout + " ms. Autoclosing.");
							jc.realClose();
							abandon(jc);
						} else {
							try {
								if (jc.conn.isClosed())
									abandon(jc);
							} catch (SQLException e) {
								abandon(jc);
							}
						}
					}
					synchronized (availableConnections) {
						while (availableConnections.size() < minIdle) {
							try {
								availableConnections.add(createNewConnection());
							} catch (SQLException e) {
								break;
							}
						}
					}
				}
			}
		} catch (InterruptedException e) {
			logger.warn(getName() + " closing.");
		}
	}

	private String getProperty(Properties properties, String key, String defaultValue) {
		String prop = properties.getProperty(key);
		if (prop == null) {
			if (defaultValue == null)
				throw new RequiredPropertyMissing(dbname, key);
			else
				prop = defaultValue;
		}
		return prop;
	}

	/**
	 * @author szalik
	 */
	class JdbcWrappedConnection implements Connection {
		private Connection conn;
		private long ts;

		public JdbcWrappedConnection(Connection c) {
			this.conn = c;
		}

		public void clearWarnings() throws SQLException {
			conn.clearWarnings();
		}

		public void close() throws SQLException {
			release(this);
		}

		public void commit() throws SQLException {
			conn.commit();
		}

		public Statement createStatement() throws SQLException {
			return conn.createStatement();
		}

		public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
			return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
		}

		public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
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

		public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
			return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		}

		public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
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

		public void realClose() {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}

		// ------------------ Methods for JDK6
		// ---------------------------------------
		public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
			throw new RuntimeException("Not supported.");
		}

		public Blob createBlob() throws SQLException {
			throw new RuntimeException("Not supported.");
		}

		public Clob createClob() throws SQLException {
			throw new RuntimeException("Not supported.");
		}

		public NClob createNClob() throws SQLException {
			throw new RuntimeException("Not supported.");
		}

		public SQLXML createSQLXML() throws SQLException {
			throw new RuntimeException("Not supported.");
		}

		public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
			throw new RuntimeException("Not supported.");
		}

		public Properties getClientInfo() throws SQLException {
			throw new RuntimeException("Not supported.");
		}

		public String getClientInfo(String name) throws SQLException {
			throw new RuntimeException("Not supported.");
		}

		public boolean isValid(int timeout) throws SQLException {
			throw new RuntimeException("Not supported.");
		}

		public void setClientInfo(Properties properties) {
			throw new RuntimeException("Not supported.");
		}

		public void setClientInfo(String name, String value) {
			throw new RuntimeException("Not supported.");
		}

		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			throw new RuntimeException("Not supported.");
		}

		public <T> T unwrap(Class<T> iface) throws SQLException {
			throw new RuntimeException("Not supported.");
		}
	}

}
