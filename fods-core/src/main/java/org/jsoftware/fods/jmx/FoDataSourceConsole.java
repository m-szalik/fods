package org.jsoftware.fods.jmx;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;

import org.jsoftware.fods.client.EventNotification;
import org.jsoftware.fods.client.FodsEventListener;
import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.client.ext.FodsDbStateStatus;
import org.jsoftware.fods.event.AbstractFodsEvent;
import org.jsoftware.fods.event.ActiveDatabaseChangedEvent;
import org.jsoftware.fods.impl.FoDataSourceImpl;
import org.jsoftware.fods.impl.FodsDbStateImpl;
import org.jsoftware.fods.impl.stats.StatisticsItem;

/**
 * JmxBean implementation of {@link FoDataSourceConsoleMBean} for {@link FoDataSourceImpl}.
 * @author szalik
 */
public class FoDataSourceConsole extends NotificationBroadcasterSupport implements NotificationEmitter, FoDataSourceConsoleMXBean, FodsEventListener {
	private FoDataSourceImpl ds;
	private long notificationSeq;
	private Configuration configuration;



	public FoDataSourceConsole(FoDataSourceImpl ds, Configuration configuration) {
		this.ds = ds;
		this.configuration = configuration;
	}



	public String testRaport() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for (Configuration.DatabaseConfiguration dbc : configuration.getDatabaseConfigurations()) {
			Connection connection = null;
			try {
				pw.append(dbc.getDatabaseName()).append(": ").append(dbc.getTestSql()).append(" - ");
				connection = dbc.getConnectionCreator().getConnection();
				PreparedStatement statement = connection.prepareStatement(dbc.getTestSql());
				statement.execute();
				statement.close();
				pw.append("OK\n");
			} catch (SQLException e) {
				pw.append("FAILED " + e.getMessage() + "\n");
				e.printStackTrace(pw);
				pw.append('\n');
			} finally {
				try {
					if (connection != null) connection.close();
				} catch (SQLException e) { /* ignore */ }
			}
		}
		pw.close();
		return sw.toString();
	}



	public boolean test(String dbName) {
		return ds.testDatabase(dbName);
	}



	public String[] getDatabaseNames() {
		return configuration.getDatabaseNames().toArray(new String[0]);
	}



	public boolean turnOffDatabase(String dbName) {
		return turnInternal(dbName, false);
	}



	public boolean turnOnDatabase(String dbName) {
		return turnInternal(dbName, true);
	}



	private boolean turnInternal(String dbName, boolean b) {
		FodsDbStateImpl dbs = ds.getFodsState().getDbstate(dbName);
		if (dbs == null) {
			thrownodb(dbName);
		}
		if ((b && dbs.getStatus() != FodsDbStateStatus.DISCONNETED) || (!b && dbs.getStatus() == FodsDbStateStatus.DISCONNETED)) {
			return false;
		}
		dbs.setState(b ? FodsDbStateStatus.VALID : FodsDbStateStatus.DISCONNETED);
		return true;
	}



	public JMXFodsDbState getCurrentDatabaseState(String dbName) {
		FodsDbStateImpl dbs = ds.getFodsState().getDbstate(dbName);
		if (dbs == null) {
			thrownodb(dbName);
		}
		long bt = dbs.getBrokenTime();
		Throwable reason = bt > 0 ? dbs.getLastException() : null;
		return new JMXFodsDbState(dbName, dbs.getStatus().name(), reason, bt > 0 ? bt : null, dbs.isReadOnly());
	}



	public JMXStatistics[] getStatistics() {
		JMXStatistics[] ret = new JMXStatistics[configuration.getDatabaseNames().size()];
		int i = 0;
		for (String dbname : configuration.getDatabaseNames()) {
			StatisticsItem si = ds.getStatistics().getItem(dbname);
			if (si == null) {
				si = new StatisticsItem();
			}
			ret[i] = si.createJMXStatistics(dbname);
			i++;
		}
		return ret;
	}



	public String getFodsName() {
		return configuration.getFoDSName();
	}



	public String getCurrentDatabaseName() {
		return ds.getFodsState().getCurrentDatabase();
	}



	public boolean forceSetCurrentDatabaseName(String name) {
		if (configuration.getDatabaseConfigurationByName(name) == null) {
			thrownodb(name);
		}
		String currentDBName = getCurrentDatabaseName();
		if (name.equals(currentDBName)) {
			return false;
		}
		ds.getFodsState().setCurrentDatabase(name);
		ds.notifyChangeEvent(new ActiveDatabaseChangedEvent(name, currentDBName));
		return true;
	}



	public void onEvent(AbstractFodsEvent event) {
		EventNotification notification = new EventNotification(event, this, notificationSeq++, System.currentTimeMillis());
		sendNotification(notification);
	}



	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		MBeanNotificationInfo mbni1 = new MBeanNotificationInfo(new String[] { EventNotification.NTYPE }, EventNotification.class.getName(), "DataSource event");
		return new MBeanNotificationInfo[] { mbni1 };
	}



	private void thrownodb(String dbName) throws NoSuchElementException {
		throw new NoSuchElementException("No database named \"" + dbName + "\"");
	}

}
