package org.gruszecm.fods.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;

import org.gruszecm.fods.stats.StatisticsItem;


public class StatsConnectionWrapper implements Connection {
	private Connection connection;
	private StatisticsItem statisticsItem;
	

	public StatsConnectionWrapper(StatisticsItem statisticsItem, Connection conn) {
		this.statisticsItem = statisticsItem;
		this.connection = conn;
	}

	public void clearWarnings() throws SQLException {
		connection.clearWarnings();
	}

	public void close() throws SQLException {
		statisticsItem.addRelease();
		connection.close();
	}

	public void commit() throws SQLException {
		connection.commit();
	}

	public Statement createStatement() throws SQLException {
		return connection.createStatement();
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return connection.createStatement(resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return connection.createStatement(resultSetType, resultSetConcurrency);
	}

	public boolean getAutoCommit() throws SQLException {
		return connection.getAutoCommit();
	}

	public String getCatalog() throws SQLException {
		return connection.getCatalog();
	}

	public int getHoldability() throws SQLException {
		return connection.getHoldability();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		return connection.getMetaData();
	}

	public int getTransactionIsolation() throws SQLException {
		return connection.getTransactionIsolation();
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return connection.getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException {
		return connection.getWarnings();
	}

	public boolean isClosed() throws SQLException {
		return connection.isClosed();
	}

	public boolean isReadOnly() throws SQLException {
		return connection.isReadOnly();
	}

	public String nativeSQL(String sql) throws SQLException {
		return connection.nativeSQL(sql);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		return connection.prepareCall(sql);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return connection.prepareStatement(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return connection.prepareStatement(sql, resultSetType,
				resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		return connection.prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		return connection.prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		return connection.prepareStatement(sql, columnNames);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		connection.releaseSavepoint(savepoint);
	}

	public void rollback() throws SQLException {
		connection.rollback();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		connection.rollback(savepoint);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connection.setAutoCommit(autoCommit);
	}

	public void setCatalog(String catalog) throws SQLException {
		connection.setCatalog(catalog);
	}

	public void setHoldability(int holdability) throws SQLException {
		connection.setHoldability(holdability);
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		connection.setReadOnly(readOnly);
	}

	public Savepoint setSavepoint() throws SQLException {
		return connection.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return connection.setSavepoint(name);
	}

	public void setTransactionIsolation(int level) throws SQLException {
		connection.setTransactionIsolation(level);
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		connection.setTypeMap(map);
	}

	// ------------------ Methods for JDK6 ---------------------------------------
	/*
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
	*/
}
