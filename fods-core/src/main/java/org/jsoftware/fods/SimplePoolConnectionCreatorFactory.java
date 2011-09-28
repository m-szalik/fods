package org.jsoftware.fods;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
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

import org.jsoftware.fods.client.ext.ConnectionCreator;
import org.jsoftware.fods.client.ext.ConnectionCreatorFactory;
import org.jsoftware.fods.client.ext.Logger;
import org.jsoftware.fods.impl.utils.PropertiesUtil;

/**
 * Creates {@link ConnectionCreator} that uses pool of {@link Connection}s.
 * <p>
 * Required configuration values:
 * <ul>
 * <li>jdbcURI - database's jdbc uri</li>
 * <li>driverClassName - jdbc {@link Driver}</li>
 * <li><i>Other jdbc connection properties.</i></li>
 * </ul>
 * Optional configuration values:
 * <ul>
 * <li>maxWait - login timeout</li>
 * <li>maxActive - maximal active connections</li>
 * <li>maxIdle - maximal idle connections</li>
 * <li>minWait - minimal idle connections</li>
 * <li>closeTimeout - time [ms] after that borrowed {@link Connection} will be
 * forced to close</li>
 * <li>setReadOnly=true - set connection as read only</li>
 * </ul>
 * </p>
 * 
 * @author szalik
 */
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

class SimplePoolConnectionCreator extends AbstractDriverManagerJdbcConnectionCreatorBase implements Runnable {
	private static final long POOL_THREAD_SLEEP = 1000 * 60 * 2;
	private int maxActive;
	private int maxIdle;
	private int minIdle;
	private long closeTimout;
	private List<JdbcWrappedConnection> activeConnections;
	private List<JdbcWrappedConnection> availableConnections;
	private Thread thread;

	public SimplePoolConnectionCreator(String dbname, Logger logger, Properties properties) {
		super(dbname, logger, properties);
		PropertiesUtil pu = new PropertiesUtil(properties, dbname);
		this.maxActive = Integer.valueOf(pu.getProperty("maxActive", "8"));
		this.maxIdle = Integer.valueOf(pu.getProperty("maxIdle", "1"));
		this.minIdle = Integer.valueOf(pu.getProperty("minIdle", "0"));
		if (maxIdle < minIdle) {
			throw new IllegalArgumentException("maxIdle < minIdle");
		}
		this.closeTimout = Integer.valueOf(pu.getProperty("closeTimeout", "-1"));
		this.thread = new Thread(this, getConnectionCreatorName() + "-cleaner");
		this.thread.setDaemon(true);
		this.activeConnections = new LinkedList<SimplePoolConnectionCreator.JdbcWrappedConnection>();
		this.availableConnections = new LinkedList<SimplePoolConnectionCreator.JdbcWrappedConnection>();
	}

	public void start() throws Exception {
		synchronized (availableConnections) {
			for (int a = 0; a < this.minIdle; a++) {
				this.availableConnections.add(createNewConnection());
			}
		}
		thread.start();
		super.start();
	}

	public void stop() {
		super.stop();
		synchronized (activeConnections) {
			for (SimplePoolConnectionCreator.JdbcWrappedConnection con : activeConnections) {
				con.realClose();
			}
		}
		availableConnections.clear();
		activeConnections.clear();
	}

	protected Connection createConnection() throws SQLException {
		JdbcWrappedConnection con = null;
		synchronized (availableConnections) {
			if (!availableConnections.isEmpty()) {
				con = availableConnections.remove(0);
			}
		}
		if (con != null && con.isClosed()) {
			final JdbcWrappedConnection con2 = con;
			doInLock(new Runnable() {
				public void run() {
					abandon(con2);
				}
			});
			con = createNewConnection();
		}
		if (con == null) {
			con = createNewConnection();
		}
		con.ts = System.currentTimeMillis();
		return con;
	}

	protected String getConnectionCreatorName() {
		return "SimplePoolConnectionCreator";
	}

	private JdbcWrappedConnection createNewConnection() throws SQLException {
		int maxLoginTimeout = DriverManager.getLoginTimeout();
		DriverManager.setLoginTimeout(maxWait);
		try {
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
		} finally {
			DriverManager.setLoginTimeout(maxLoginTimeout);
		}
	}

	
	/**
	 * WARN do in lock only @see {@link #doInLock(Runnable)}
	 */
	private void abandon(JdbcWrappedConnection con) {
		activeConnections.remove(con);
		availableConnections.remove(con);
		logger.debug("Connection to \"" + dbname + "\" abandoned.");
	}
	

	void release(JdbcWrappedConnection conn) {
		conn.ts = 0;
		synchronized (availableConnections) {
			if (!conn.abandoned) {
				availableConnections.add(conn);
			}
		}
	}

	public void run() {
		try {
			while (true) {
				if (!isConnectorCreatorActive())
					break;
				Thread.sleep(POOL_THREAD_SLEEP);
				if (!isConnectorCreatorActive())
					break;
				if (closeTimout > 0) {
					long ts = System.currentTimeMillis() - closeTimout;
					List<JdbcWrappedConnection> newActiveList = new LinkedList<SimplePoolConnectionCreator.JdbcWrappedConnection>();
					synchronized (activeConnections) {
						newActiveList.addAll(activeConnections);
					}
					// check for release timeouts
					boolean ab = false;
					for (final JdbcWrappedConnection jc : newActiveList) {
						if (jc.ts > 0 && jc.ts < ts) {
							logger.warn("Connection " + jc + " not released in " + closeTimout + " ms. Autoclosing.");
							jc.realClose();
							ab = true;
						} else {
							try {
								if (jc.conn.isClosed()) {
									ab = true;
								}
							} catch (SQLException e) {
								ab = true;
							}
						}
						if (ab) {
							doInLock(new Runnable() {
								public void run() {
									abandon(jc);
								}
							});
						}
					}
				}
				// check idle
				final int avc = availableConnections.size();
				// check min
				if (avc < minIdle) {
					doInLock(new Runnable() {
						public void run() {
							for (int a = avc; a < minIdle; a++) {
								try {
									availableConnections.add(createNewConnection());
								} catch (SQLException e) {
									logger.warn("Can not create new connection to database \"" + dbname + "\".");
									break;
								}
							}
						}
					});
				}
				// check max
				if (avc > maxIdle) {
					doInLock(new Runnable() {
						public void run() {
							for (int a = avc; a > maxIdle; a--) {
								if (availableConnections.size() > 0) {
									JdbcWrappedConnection con = availableConnections.get(0);
									abandon(con);
								}
							}
						}
					});
				}
			} // thread.run try
		} catch (InterruptedException e) {
			logger.warn(Thread.currentThread().getName() + " closing.");
		}
	}

	private void doInLock(Runnable op) {
		synchronized (activeConnections) {
			synchronized (activeConnections) {
				op.run();
			}
		}
	}

	/**
	 * @author szalik
	 */
	class JdbcWrappedConnection implements Connection {
		private Connection conn;
		private long ts;
		private boolean abandoned;

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

		// ------------------ Methods for JDK6----------------------------
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
