package org.jsoftware.fods.jmx;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;

import org.jsoftware.fods.client.ChangeEventListener;
import org.jsoftware.fods.client.EventNotification;
import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.event.AbstractChangeEvent;
import org.jsoftware.fods.impl.FODataSource;
import org.jsoftware.fods.stats.Statistics;


/**
 * Jmx Bean 
 * @author szalik
 */
public class FODataSourceConsole extends NotificationBroadcasterSupport implements NotificationEmitter, FODataSourceConsoleMBean, ChangeEventListener {
	private FODataSource ds;
	private long notificationSeq;
	private Configuration configuration;
	
	public FODataSourceConsole(FODataSource ds, Configuration configuration) {
		this.ds = ds;
		this.configuration = configuration;
	}

	
	public String test() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for(Configuration.DatabaseConfiguration dbc : configuration.getDatabaseConfigurations()) {
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
	
	public Statistics getStatistics() {
		return ds.getStatistics();
	}

	public String getFodsName() {
		return configuration.getFoDSName();
	}


	public String getCurrentDatabaseName() {
		return ds.getFodsState().getCurrentDatabase();
	}


	public void setCurrentDatabaseName(String name) {
		ds.getFodsState().setCurrentDatabase(name);
	}


	public void onEvent(AbstractChangeEvent event) {		
		EventNotification notification = new EventNotification(event, this, notificationSeq++, System.currentTimeMillis());
		sendNotification(notification);
	}
	
	
	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		MBeanNotificationInfo mbni1 = new MBeanNotificationInfo(new String[] { EventNotification.NTYPE }, EventNotification.class.getName(), "DataSource event");
		return new MBeanNotificationInfo[] { mbni1 };
	}




}
