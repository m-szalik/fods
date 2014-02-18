package org.jsoftware.fods.impl;

import org.jsoftware.fods.client.FodsEventListener;
import org.jsoftware.fods.client.ext.*;
import org.jsoftware.fods.event.*;
import org.jsoftware.fods.impl.stats.Statistics;
import org.jsoftware.fods.impl.stats.StatisticsItem;
import org.jsoftware.fods.impl.stats.StatsConnectionWrapper;
import org.jsoftware.fods.log.LoggerWriter;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Fail over {@link DataSource} implementation.
 * @author szalik
 */
public final class FoDataSourceImpl implements DataSource, Closeable {
	private Logger logger;
	private PrintWriter printWriter;
	private Configuration configuration;
	private Statistics stats;
	private ChangeEventsThread eventsSenderThread;
	private FodsStateImpl fodsState;
	private boolean active;



	FoDataSourceImpl(Configuration configuration) {
		this.logger = configuration.getLogger();
		this.configuration = configuration;
		this.printWriter = new PrintWriter(new LoggerWriter(logger));
		if (!Boolean.valueOf(configuration.getProperty("disableEvents"))) {
			this.eventsSenderThread = new ChangeEventsThread(configuration.getLogger());
			this.eventsSenderThread.start(); // this is the reason why this class is final
		}
		stats = new Statistics(configuration.getDatabaseNames());
		this.fodsState = new FodsStateImpl(configuration.getDatabaseNames());
	}



	public FodsStateImpl getFodsState() {
		return fodsState;
	}



	public Statistics getStatistics() {
		return stats;
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.DataSource#getConnection()
	 */
	public Connection getConnection() throws SQLException {
		return connection();
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.DataSource#getConnection(java.lang.String,
	 * java.lang.String)
	 */
	public Connection getConnection(String username, String password) throws SQLException {
		return connection();
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.DataSource#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws SQLException {
		return printWriter;
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.DataSource#getLoginTimeout()
	 */
	public int getLoginTimeout() throws SQLException {
		// return creator.getLoginTimeout();
		return 10;
	}

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return java.util.logging.Logger.getLogger(getClass().getName());
    }


    /*
     * (non-Javadoc)
     *
     * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
     */
	public void setLogWriter(PrintWriter out) throws SQLException {
		printWriter = out;
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.DataSource#setLoginTimeout(int)
	 */
	public void setLoginTimeout(int seconds) throws SQLException {
		// checkInit();
		// creator.setLoginTimeout(seconds);
	}



	public Boolean testDatabase(String dbName) {
		Configuration.DatabaseConfiguration dbc = configuration.getDatabaseConfigurationByName(dbName);
		Boolean b = null;
		if (dbc != null) {
			Connection connection = getConnectionFromDb(dbName);
			b = (connection != null);
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {}
		}
		return b;
	}



	public void start() {
		for (Configuration.DatabaseConfiguration dbc : configuration.getDatabaseConfigurations()) {
			try {
				dbc.getConnectionCreator().start();
			} catch (Exception e) {
				throw new RuntimeException("Error invoking start() of " + dbc.getConnectionCreator(), e);
			}
		}
		active = true;
	}



	public void stop() {
		for (Configuration.DatabaseConfiguration dbc : configuration.getDatabaseConfigurations()) {
			try {
				dbc.getConnectionCreator().stop();
			} catch (Exception e) {}
		}
		active = false;
	}



	public void close() throws IOException {
		stop();
	}



	/**
	 * @return
	 * @throws SQLException
	 */
	private Connection connection() throws SQLException {
		if (!active) throw new SQLException("DataSource is not active / closed.");
		Selector selector = configuration.getSelector();
		String dbname;
		Connection connection;
		do {
			dbname = selector.select(fodsState);
			if (dbname == null) break;
			connection = getConnectionFromDb(dbname);
			if (connection != null) {
				if (!dbname.equals(fodsState.getCurrentDatabase())) {
					notifyChangeEvent(new ActiveDatabaseChangedEvent(dbname, fodsState.getCurrentDatabase()));
					fodsState.setCurrentDatabase(dbname);
				}
				return wrap(connection, dbname);
			}
		} while (dbname != null);
		notifyChangeEvent(new NoMoreDatabasesEvent());
		throw new SQLException("No more databases avaialable.");
	}



	private Connection getConnectionFromDb(String dbname) {
		FodsDbStateImpl dbState = fodsState.getDbstate(dbname);
		FodsDbStateStatus newStatus = dbState.getStatus();
		Connection connection;
		try {
			if (dbState.getStatus() == FodsDbStateStatus.BROKEN) {
				notifyChangeEvent(new RecoveryStartEvent(dbname));
			}
			// test sql
			Configuration.DatabaseConfiguration dbc = configuration.getDatabaseConfigurationByName(dbname);
			connection = dbc.getConnectionCreator().getConnection();
			PreparedStatement statement = connection.prepareStatement(dbc.getTestSql());
			try {
				statement.execute();
			} finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {}
				}
			}
			//
			if (dbState.getStatus() == FodsDbStateStatus.BROKEN) {
				notifyChangeEvent(new RecoverySucessEvent(dbname));
			}
			newStatus = FodsDbStateStatus.VALID;
		} catch (SQLException e) {
			dbState.setLastException(e);
			connection = null;
			if (dbState.getStatus() == FodsDbStateStatus.BROKEN) {
				notifyChangeEvent(new RecoveryFailedEvent(dbname, e));
			} else {
				notifyChangeEvent(new DatabaseFiledEvent(dbname, e));
			}
			newStatus = FodsDbStateStatus.BROKEN;
		}
		if (dbState.getStatus() != newStatus) {
			notifyChangeEvent(new DatabaseStatusChangedEvent(dbname, dbState.getStatus(), newStatus));
		}
		dbState.setState(newStatus);
		if (connection != null) {
			try {
				dbState.setReadonly(connection.isReadOnly());
			} catch (SQLException e) {
				logger.log(LogLevel.DEBUG, "Error obtaining readOnly information from " + dbname, e);
			}
		}
		return connection;
	}



	private Connection wrap(Connection con, String dbName) {
		Connection connection;
		if (configuration.isEnableStats()) {
			StatisticsItem statisticsItem = stats.getItem(dbName);
			connection = new StatsConnectionWrapper(statisticsItem, con, configuration.getLogLongSqls(), logger, dbName);
		} else {
			connection = con;
		}
		return connection;
	}



	public void addChangeEventListener(FodsEventListener listener) {
		if (eventsSenderThread != null) {
			eventsSenderThread.addChangeEventListener(listener);
		}
	}



	public void notifyChangeEvent(AbstractFodsEvent event) {
		if (eventsSenderThread != null) {
			eventsSenderThread.notifyChangeEvent(event);
		}
		if (configuration.isEnableStats()) {
			if (event instanceof RecoverySucessEvent) {
				stats.getItem(event.getDbName()).addRecovery();
			}
			if (event instanceof DatabaseFiledEvent) {
				stats.getItem(event.getDbName()).addBreak();
			}
		}
		if (event instanceof DatabaseFiledEvent) {
			DatabaseFiledEvent dbfEvent = (DatabaseFiledEvent) event;
			logger.log(LogLevel.WARN, "Database " + event.getDbName() + " failed - " + dbfEvent.getReason(), dbfEvent.getReason());
		}
		logger.logEvent(event);
	}



	// ------------------ Methods for JDK6 ---------------------------------------
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new RuntimeException("Not supported.");
	}



	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new RuntimeException("Not supported.");
	}
}
