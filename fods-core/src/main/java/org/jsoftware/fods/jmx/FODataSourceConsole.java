package org.jsoftware.fods.jmx;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;

import org.jsoftware.fods.client.EventNotification;
import org.jsoftware.fods.client.FodsEventListener;
import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.client.ext.FodsDbState;
import org.jsoftware.fods.client.ext.FodsDbStateStatus;
import org.jsoftware.fods.event.AbstractFodsEvent;
import org.jsoftware.fods.impl.FODataSource;
import org.jsoftware.fods.stats.Statistics;

/**
 * JmxBean implementation of {@link FODataSourceConsoleMBean} for
 * {@link FODataSource}.
 * @author szalik
 */
public class FODataSourceConsole extends NotificationBroadcasterSupport implements NotificationEmitter, FODataSourceConsoleMBean, FodsEventListener {
	private FODataSource ds;
	private long notificationSeq;
	private Configuration configuration;

	public FODataSourceConsole(FODataSource ds, Configuration configuration) {
		this.ds = ds;
		this.configuration = configuration;
	}

	public String testRaport() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for (Configuration.DatabaseConfiguration dbc : configuration.getDatabaseConfigurations()) {
			try {
				pw.append(dbc.getDatabaseName()).append(": ").append(dbc.getTestSql()).append(" - ");
				Connection connection = dbc.getConnectionCreator().getConnection();
				PreparedStatement statement = connection.prepareStatement(dbc.getTestSql());
				statement.execute();
				pw.append("OK\n");
			} catch (SQLException e) {
				pw.append("FAILED " + e.getMessage() + "\n");
				e.printStackTrace(pw);
				pw.append('\n');
			}
		}
		pw.close();
		return sw.toString();
	}

	public Boolean test(String dbName) {
		Configuration.DatabaseConfiguration dbc = configuration.getDatabaseConfigurationByName(dbName);
		Boolean b = null;
		if (dbc != null) {
			try {
				Connection connection = dbc.getConnectionCreator().getConnection();
				PreparedStatement statement = connection.prepareStatement(dbc.getTestSql());
				statement.execute();
				b = Boolean.TRUE;
			} catch (SQLException e) {
				b = Boolean.FALSE;
			}
		}
		return b;
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
		FodsDbState dbs = ds.getFodsState().getDbstate(dbName);
		if (dbs == null) {
			return false;
		}
		if ((b && dbs.getStatus() != FodsDbStateStatus.DISCONNETED) || (!b && dbs.getStatus() == FodsDbStateStatus.DISCONNETED)) {
			return false;
		}
		dbs.setState(b ? FodsDbStateStatus.VALID : FodsDbStateStatus.DISCONNETED);
		return true;
	}

	public FodsDbState getCurrentDatabaseState(String dbName) {
		return ds.getFodsState().getDbstate(dbName);
	}

	public Statistics getStatistics() {
		return ds.getStatistics();
	}

	public String getFodsName() {
		return configuration.getFoDSName();
	}

	public String getCurrentDatabaseName() {
		return ds.getFodsState().getCurrentDatabase();
	}

	public boolean forceSetCurrentDatabaseName(String name) {
		if (configuration.getDatabaseConfigurationByName(name) == null) {
			return false;
		}
		if (getCurrentDatabaseName().equals(name)) {
			return false;
		}
		ds.getFodsState().setCurrentDatabase(name);
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

}
