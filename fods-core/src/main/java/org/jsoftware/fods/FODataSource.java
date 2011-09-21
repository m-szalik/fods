package org.jsoftware.fods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jsoftware.fods.client.ChangeEventListener;
import org.jsoftware.fods.client.ext.DatabasesState;
import org.jsoftware.fods.client.ext.Selector;
import org.jsoftware.fods.event.AbstractChangeEvent;
import org.jsoftware.fods.event.DatabaseFiledChangeEvent;
import org.jsoftware.fods.event.IndexChangedChangeEvent;
import org.jsoftware.fods.event.NoMoreDataSourcesChangeEvent;
import org.jsoftware.fods.event.RecoveryCanceledChangeEvent;
import org.jsoftware.fods.event.RecoveryChangeEvent;
import org.jsoftware.fods.event.RecoveryFiledChangeEvent;
import org.jsoftware.fods.impl.RecovererFactory;
import org.jsoftware.fods.impl.StatsConnectionWrapper;
import org.jsoftware.fods.impl.StatsListener;
import org.jsoftware.fods.log.LogLevel;
import org.jsoftware.fods.log.Logger;
import org.jsoftware.fods.log.LoggerWriter;
import org.jsoftware.fods.stats.Statistics;
import org.jsoftware.fods.stats.StatisticsItem;


/**
 * Failover {@link DataSource}
 * <p>
 * Check if primary {@link DataSource} is available if not return next one.
 * </p>
 * @author szalik
 */
public class FODataSource implements DataSource {
	private static final String NO_MORE_DATASOURCE_MESSAGE = "No more dataSources available!";
	private ConnectionCreator creator;
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

	
	public FODataSource(Selector selector, Logger log, ConnectionCreator connectionCreator, Configuration configuration) {
		int nodb = connectionCreator.numOfDatabases();
		this.creator = connectionCreator;
		this.logger = log;
		this.selector = selector;
		this.configuration = configuration;
		this.printWriter = new PrintWriter(new LoggerWriter(logger));
		this.eventsSenderThread = new ChangeEventsThread(log);
		displayInfo(configuration, connectionCreator, selector);
		this.eventsSenderThread.start();
		this.state = new DatabasesState(nodb);
		this.prevIndex = -1;
		if (configuration.isEnableStats()) {
			stats = new Statistics(nodb);
			addChangeEventListener(new StatsListener(stats));
		}
	}
	
	private static void displayInfo(Configuration configuration, ConnectionCreator connectionCreator, Selector selector) {
		// display information
		try {
			InputStream ins = FODataSource.class.getResourceAsStream("/org/jsoftware/fods/message.txt");
			if (ins != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(ins));
				StringBuilder out = new StringBuilder();
				String s;
				while ((s = br.readLine()) != null) {
					out.append(s).append('\n');
				}
				s = out.toString();
				s = s.replaceAll("%connectionCreator%", connectionCreator == null ? "-" : connectionCreator.getClass().getSimpleName());
				s = s.replaceAll("%databaseSelector%", selector == null ? "-" : selector.getClass().getSimpleName());
				if (s.length() > 0) {
					System.out.println(s);
				}
				br.close();
			} // if ins
		} catch (IOException e) { 	}
	}
	
	public Statistics getStatistics() {
		return stats;
	}
	
	public ConnectionCreator getConnectionCreator() {
		return creator;
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
		return creator.getLoginTimeout();
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
		creator.setLoginTimeout(seconds);
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
		Connection connection = creator.getConnection(index);
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
			for(int i = 0; i<creator.numOfDatabases(); i++) {
				if (! state.isBroken(i)) continue;
				logger.debug("Checking if the dataSource " + creator.getConnectionId(i) + " is back.");
				try { 
					test(i);
					notifyChangeEvent(new RecoveryChangeEvent(i));
					logger.info("DataSource " + creator.getConnectionId(i) + " is back. :-)");
					state.clearReason(i);
					stateChanged = true;
				} catch (SQLException e) {
					isBroken = true;
					notifyChangeEvent(new RecoveryFiledChangeEvent(i, e));
					logger.debug("DataSource " + creator.getConnectionId(i) + " is still broken.");
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
				logger.log(LogLevel.WARN, "DataSource " + creator.getConnectionId(index) + " is broken!", e);
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
				logger.info("DataSource set to " + creator.getConnectionId(index) + " by user");
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

