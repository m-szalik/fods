package org.jsoftware.fods.impl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jsoftware.fods.ChangeEventsThread;
import org.jsoftware.fods.client.ChangeEventListener;
import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.client.ext.FodsDbState;
import org.jsoftware.fods.client.ext.FodsDbState.STATE;
import org.jsoftware.fods.client.ext.FodsState;
import org.jsoftware.fods.client.ext.Logger;
import org.jsoftware.fods.client.ext.Selector;
import org.jsoftware.fods.event.AbstractChangeEvent;
import org.jsoftware.fods.event.DatabaseChangedChangeEvent;
import org.jsoftware.fods.event.DatabaseFiledChangeEvent;
import org.jsoftware.fods.event.NoMoreDataSourcesChangeEvent;
import org.jsoftware.fods.event.RecoveryFailedEvent;
import org.jsoftware.fods.event.RecoveryStartEvent;
import org.jsoftware.fods.event.RecoverySucessEvent;
import org.jsoftware.fods.log.LoggerWriter;
import org.jsoftware.fods.stats.Statistics;
import org.jsoftware.fods.stats.StatisticsItem;

/**
 * Failover {@link DataSource}
 * <p>
 * Check if primary {@link DataSource} is available if not return next one.
 * </p>
 * 
 * @author szalik
 */
public class FODataSource implements DataSource {
	private Logger logger;
	private PrintWriter printWriter;
	private Configuration configuration;
	private Statistics stats;
	private ChangeEventsThread eventsSenderThread;
	private FodsState fodsState;

	FODataSource(Configuration configuration) {
		this.logger = configuration.getLogger();
		this.configuration = configuration;
		this.printWriter = new PrintWriter(new LoggerWriter(logger));
		if (! Boolean.valueOf(configuration.getProperty("disableEvents"))) {
			this.eventsSenderThread = new ChangeEventsThread(configuration.getLogger());
			this.eventsSenderThread.start();
		}
		if (configuration.isEnableStats()) {
			if (this.eventsSenderThread == null) {
				logger.warn("Can not setup stats without events turned on. Set property \"disableEvents\" to false.");
			} else {
				stats = new Statistics(configuration.getDatabaseNames());
				addChangeEventListener(new StatsListener(stats));
			}
		}
		this.fodsState = new FodsState(configuration.getDatabaseNames()); 
	}
	
	public FodsState getFodsState() {
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

	private Connection test(String dbname) throws SQLException {
		Configuration.DatabaseConfiguration dbc = configuration.getDatabaseConfigurationByName(dbname);
		Connection connection = dbc.getConnectionCreator().getConnection();
		PreparedStatement statement = connection.prepareStatement(dbc.getTestSql());
		statement.execute();
		return connection;
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	private Connection connection() throws SQLException {
		Selector selector = configuration.getSelector();
		String dbname;
		do {
			dbname = selector.select(fodsState);
			if (dbname == null) break;
			FodsDbState dbState = fodsState.getDbstate(dbname);
			try {
				if (dbState.getState() == STATE.BROKEN) {
					notifyChangeEvent(new RecoveryStartEvent(dbname));
				}
				Connection con = test(dbname);
				if (dbState.getState() == STATE.BROKEN) {
					notifyChangeEvent(new RecoverySucessEvent(dbname));
				}
				dbState.setState(STATE.VALID);
				if (! dbname.equals(fodsState.getCurrentDatabase())) {
					notifyChangeEvent(new DatabaseChangedChangeEvent(dbname, fodsState.getCurrentDatabase()));
					fodsState.setCurrentDatabase(dbname); 
				}
				return wrap(con, dbname);
			} catch (SQLException e) {
				if (dbState.getState() == STATE.BROKEN) {
					notifyChangeEvent(new RecoveryFailedEvent(dbname, e));
				} else {
					notifyChangeEvent(new DatabaseFiledChangeEvent(dbname, e));
				}
				dbState.setState(STATE.BROKEN); 
			}
		} while (dbname != null);
		notifyChangeEvent(new NoMoreDataSourcesChangeEvent());
		throw new SQLException("No more connections avaialable");
	}

	private Connection wrap(Connection con, String dbName) {
		Connection connection;
		if (configuration.isEnableStats()) {
			StatisticsItem statisticsItem = stats.getItem(dbName);
			connection = new StatsConnectionWrapper(statisticsItem, con);
		} else {
			connection = con;
		}
		return connection;
	}

	public void addChangeEventListener(ChangeEventListener listener) {
		if (eventsSenderThread != null) {
			eventsSenderThread.addChangeEventListener(listener);
		}
	}

	private void notifyChangeEvent(AbstractChangeEvent event) {
		if (eventsSenderThread != null) {	
			eventsSenderThread.notifyChangeEvent(event);
		}
	}

	// ------------------ Methods for JDK6
	// ---------------------------------------
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new RuntimeException("Not supported.");
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new RuntimeException("Not supported.");
	}

}
