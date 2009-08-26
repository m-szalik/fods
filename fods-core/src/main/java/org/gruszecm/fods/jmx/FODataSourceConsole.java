package org.gruszecm.fods.jmx;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.sql.DataSource;

import org.gruszecm.fods.Configuration;
import org.gruszecm.fods.FODataSource;
import org.gruszecm.fods.Recoverer;
import org.gruszecm.fods.client.ChangeEventListener;
import org.gruszecm.fods.client.EventNotification;
import org.gruszecm.fods.event.AbstractChangeEvent;
import org.gruszecm.fods.event.AbstractFailedChangeEvent;
import org.gruszecm.fods.event.IndexChangedChangeEvent;
import org.gruszecm.fods.event.NoMoreDataSourcesChangeEvent;
import org.gruszecm.fods.event.RecoveryChangeEvent;
import org.gruszecm.fods.jmx.FODataSourceConsoleMBean;
import org.gruszecm.fods.stats.Statistics;


/**
 * 
 * @author szalik
 */
public class FODataSourceConsole extends NotificationBroadcasterSupport implements NotificationEmitter, FODataSourceConsoleMBean, ChangeEventListener {
	private FODataSource ds;
	private volatile Throwable lastPrimaryDatabaseFailReason;
	private int currentIndex = -1;
	private long notificationSeq;
	private Boolean currentConnectionReadOnly;
	
	public FODataSourceConsole(FODataSource ds) {
		this.ds = ds;
	}

	public int getBackTime() {
		return ds.getConfiguration().getBackTime();
	}

	public int getCurrentDataSourceIndex() {
		return currentIndex;
	}
	
	public Boolean getCurrentConnectionReadOnly() {
		if (currentConnectionReadOnly == null && currentIndex >= 0) {
			currentConnectionReadOnly = isConnectionReadOnly(currentIndex);
		}
		return currentConnectionReadOnly;
	}
	
	public String getCurrentDataSourceName() {
		int ind = currentIndex;
		if (ind >= ds.getConfiguration().size() || ind<0) {
			return "";
		} else {
			return ds.getConfiguration().getDataSourceName(ind);
		}
	}

	public String getLastBrokenReason() {
		return lastPrimaryDatabaseFailReason != null ? lastPrimaryDatabaseFailReason.toString() : "";
	}

	public boolean isAutoRecovery() {
		return ds.getConfiguration().isAutoRecovery();
	}

	public void setAutoRecovery(boolean b) {
		ds.getConfiguration().setAutoRecovery(b);
		if (! b) {
			ds.cancleCurrentRecoveryProcedure();
		}
	}
	
	public Integer getNextRecoveryCountdown() {
		Recoverer r = ds.getRecoverer();
		if (r != null) {
			return Integer.valueOf((int)(r.getRecoveryTimestamp() - System.currentTimeMillis()) / 1000);
		}
		return null;
	}

	public void setBackTime(int sec) {
		ds.getConfiguration().setBackTime(sec);
	}

	public void setCurrentDataSourceIndex(int index) {
		ds.changeIndexTo(index);
	}
	
	public Boolean isConnectionReadOnly(int index) {
		DataSource dataSource = ds.getConfiguration().getDataSource(index);
		try {
			return Boolean.valueOf(dataSource.getConnection().isReadOnly());
		} catch (SQLException e) {
			return null;	
		}
	}
	
	public String test() {
		Configuration conf = ds.getConfiguration();
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		String sql = conf.getTestSql();
		pw.append("Test SQL: ").append(sql).append('\n');
		for(int i=0; i<conf.size(); i++) {
			try {
				pw.append(conf.getDataSourceName(i)).append(": ");
				Connection connection = conf.getDataSource(i).getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
				statement.execute();
				pw.append("OK\n");
			} catch (SQLException e) {
				pw.append("FAILED\n");
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

	public void onEvent(AbstractChangeEvent event) {
		if (event instanceof IndexChangedChangeEvent) {
			IndexChangedChangeEvent e = (IndexChangedChangeEvent) event;
			currentIndex = e.getIndex();
			currentConnectionReadOnly = isConnectionReadOnly(currentIndex);
		}
		if (event.getIndex() == 0 && event instanceof AbstractFailedChangeEvent) {
			AbstractFailedChangeEvent e = (AbstractFailedChangeEvent) event;
			lastPrimaryDatabaseFailReason = e.getReason();
		}
		if (event.getIndex() == 0 && event instanceof RecoveryChangeEvent) {
			lastPrimaryDatabaseFailReason = null;
		}
		if (event instanceof NoMoreDataSourcesChangeEvent) {
			currentIndex = -1;
			currentConnectionReadOnly = null;
		}
		
		EventNotification notification = new EventNotification(event, this, notificationSeq++, System.currentTimeMillis());
		sendNotification(notification);
	}
	
	
	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		MBeanNotificationInfo mbni1 = new MBeanNotificationInfo(new String[] { EventNotification.NTYPE }, EventNotification.class.getName(), "DataSource event");
		return new MBeanNotificationInfo[] { mbni1 };
	}

}
