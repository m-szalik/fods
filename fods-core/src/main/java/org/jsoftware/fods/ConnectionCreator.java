package org.jsoftware.fods;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.jsoftware.fods.log.Logger;

/**
 * @author szalik
 * 
 */
public interface ConnectionCreator {

	int numOfDatabases();

	String getConnectionId(int i);

	Connection getConnection(int i) throws SQLException;

	void markAbandonedConnection(int i, Connection connection);

	int getLoginTimeout() throws SQLException;

	void setLoginTimeout(int seconds) throws SQLException;

	void init(Logger logger, Properties properties) throws Exception;

}
