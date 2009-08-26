package org.gruszecm.fods;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.gruszecm.fods.client.ChangeEventListener;
import org.gruszecm.fods.client.ext.DatabasesState;
import org.gruszecm.fods.client.ext.Selector;
import org.gruszecm.fods.event.AbstractChangeEvent;
import org.gruszecm.fods.event.DatabaseFiledChangeEvent;
import org.gruszecm.fods.event.IndexChangedChangeEvent;
import org.gruszecm.fods.event.NoMoreDataSourcesChangeEvent;
import org.gruszecm.fods.event.RecoveryCanceledChangeEvent;
import org.gruszecm.fods.event.RecoveryChangeEvent;
import org.gruszecm.fods.event.RecoveryFiledChangeEvent;
import org.gruszecm.fods.impl.RecovererFactory;
import org.gruszecm.fods.impl.StatsConnectionWrapper;
import org.gruszecm.fods.impl.StatsListener;
import org.gruszecm.fods.log.LogLevel;
import org.gruszecm.fods.log.Logger;
import org.gruszecm.fods.log.LoggerWriter;
import org.gruszecm.fods.stats.Statistics;
import org.gruszecm.fods.stats.StatisticsItem;


/**
 * Failover {@link DataSource}
 * <p>
 * Check if primary {@link DataSource} is available if not return next one.
 * </p>
 * @author szalik
 */
public class FODataSource implements DataSource {
	private static final String NO_MORE_DATASOURCE_MESSAGE = "No more dataSources available!";
	
	private Logger logger;
	private PrintWriter printWriter;
	private Configuration configuration;
	private DatabasesState state;
	private Statistics stats;
	private boolean configurationInitializated = false;
	private volatile Recoverer recoverer = null;
	private int prevIndex;
	private ChangeEventsThread eventsSenderThread;
	private Selector selector;

	
	public FODataSource(Selector selector, Logger log, Configuration configuration) {
		this.logger = log;
		this.selector = selector;
		this.configuration = configuration;
		this.printWriter = new PrintWriter(new LoggerWriter(logger));
		this.eventsSenderThread = new ChangeEventsThread(log);
		this.eventsSenderThread.start();
		this.state = new DatabasesState(configuration.size());
		this.prevIndex = -1;
		if (configuration.isEnableStats()) {
			stats = new Statistics(configuration.size());
			addChangeEventListener(new StatsListener(stats));
		}
	}
	
	public Statistics getStatistics() {
		return stats;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.sql.DataSource#getConnection()
	 */
	public Connection getConnection() throws SQLException {
		return connection();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
	 */
	public Connection getConnection(String username, String password) throws SQLException {
		return connection();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.sql.DataSource#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws SQLException {
		return printWriter;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.sql.DataSource#getLoginTimeout()
	 */
	public int getLoginTimeout() throws SQLException {
		return configuration.getDataSource(0).getLoginTimeout();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter(PrintWriter out) throws SQLException {
		printWriter = out;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.sql.DataSource#setLoginTimeout(int)
	 */
	public void setLoginTimeout(int seconds) throws SQLException {
		checkInit();
		for(int i=0; i<configuration.size(); i++) {
			configuration.getDataSource(i).setLoginTimeout(seconds);
		}
	}

	
	private void checkInit() {
		if (! configurationInitializated) {
			configuration.init(logger);
			selector.init(state);
			if (selector instanceof ChangeEventListener) {
				addChangeEventListener((ChangeEventListener) selector);
			}
			notifyChangeEvent(new IndexChangedChangeEvent(0, -1));
			configurationInitializated = true;
		}
	}
	
	private Connection test(int index) throws SQLException {
		Connection connection = configuration.getDataSource(index).getConnection();
		PreparedStatement statement = connection.prepareStatement(configuration.getTestSql());
		statement.execute();
		return connection;
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	private Connection connection() throws SQLException {
		checkInit();
		boolean stateChanged = false;
		
		// check if try to back
		if (recoverer != null && recoverer.canRecovery()) {
			boolean isBroken = false;
			for(int i = 0; i<configuration.size(); i++) {
				if (! state.isBroken(i)) continue;
				logger.debug("Checking if the dataSource " + configuration.getDataSourceName(i) + " is back.");
				try { 
					test(i);
					notifyChangeEvent(new RecoveryChangeEvent(i));
					logger.info("DataSource " + configuration.getDataSourceName(i) + " is back. :-)");
					state.clearReason(i);
					stateChanged = true;
				} catch (SQLException e) {
					isBroken = true;
					notifyChangeEvent(new RecoveryFiledChangeEvent(i, e));
					logger.debug("DataSource " + configuration.getDataSourceName(i) + " is still broken.");
					recoverer.recoveryFailed();
					state.setReakReason(i, e);
					stateChanged = true;
				}
			}
			if (! isBroken) recoverer = null;
			if (stateChanged) selector.onStateChanged();
			stateChanged = false;
		}
		
		Connection conn = null;		
		// get spare connection
		int index;
		do {
			index = selector.select();
			try {
				if (! state.isBroken(index)) {
					conn = test(index);
					break;
				}
			} catch (SQLException e) {
				logger.log(LogLevel.WARN, "DataSource " + configuration.getDataSourceName(index) + " is broken!", e);
				state.setReakReason(index, e);
				selector.onStateChanged();
				if (recoverer == null) {
					recoverer = RecovererFactory.getRecoverer(configuration);
				}
				notifyChangeEvent(new DatabaseFiledChangeEvent(index, e));
			}
			index++;
		} while(index < state.size());
		
		if (conn == null) {
			logger.log(LogLevel.CRITICAL, NO_MORE_DATASOURCE_MESSAGE);
			notifyChangeEvent(new NoMoreDataSourcesChangeEvent());
			throw new SQLException(NO_MORE_DATASOURCE_MESSAGE);
		} else {
			if (prevIndex != index) {
				notifyChangeEvent(new IndexChangedChangeEvent(index,prevIndex));
				logger.debug("Current index is changed " + prevIndex + " --> " + index);
				prevIndex = index;
			}
			if (stats != null) {
				StatisticsItem statisticsItem = stats.getItem(index);
				statisticsItem.addGet();
				conn = new StatsConnectionWrapper(statisticsItem, conn);
			}
			return conn;
		}
	}

	public Recoverer getRecoverer() {
		return recoverer;
	}
	
	public void cancleCurrentRecoveryProcedure() {
		if (recoverer != null) {
			recoverer = null;
			logger.info("Recovery procedure canceled");
			for(int i=0; i<state.size(); i++) {
				if (state.isBroken(i)) {
					notifyChangeEvent(new RecoveryCanceledChangeEvent(i));
				}
			}  // for
		}
	}
	
	public Configuration getConfiguration() {
		if (configurationInitializated) return configuration;
		throw new IllegalStateException("wait for full init");
	}

	public void changeIndexTo(int index) {
		if (index != prevIndex) {
			try {
				selector.forceSetIndex(index);
				logger.info("DataSource set to " + configuration.getDataSourceName(index) + " by user");
				notifyChangeEvent(new IndexChangedChangeEvent(index, prevIndex));
				prevIndex = index;
			} catch (UnsupportedOperationException e) {
				logger.log(LogLevel.WARN, "Change index is not supported by " + selector);
			}
		}
	}

	public void addChangeEventListener(ChangeEventListener listener) {
		eventsSenderThread.addChangeEventListener(listener);
	}
	
	private void notifyChangeEvent(AbstractChangeEvent event) {
		eventsSenderThread.notifyChangeEvent(event);
	}

// ------------------ Methods for JDK6 ---------------------------------------
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new RuntimeException("Not supported.");
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new RuntimeException("Not supported.");
	}
}

