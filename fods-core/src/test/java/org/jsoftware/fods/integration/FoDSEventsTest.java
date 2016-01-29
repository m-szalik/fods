package org.jsoftware.fods.integration;

import org.jsoftware.fods.AbstractDbTestTemplate;
import org.jsoftware.fods.client.EventNotification;
import org.jsoftware.fods.client.ext.FodsDbStateStatus;
import org.jsoftware.fods.event.AbstractFodsEvent;
import org.jsoftware.fods.event.ActiveDatabaseChangedEvent;
import org.jsoftware.fods.event.DatabaseFiledEvent;
import org.jsoftware.fods.event.DatabaseStatusChangedEvent;
import org.jsoftware.fods.event.NoMoreDatabasesEvent;
import org.jsoftware.fods.impl.AbstractFoDataSourceFactory;
import org.jsoftware.fods.jmx.FoDataSourceConsoleMXBean;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FoDSEventsTest extends AbstractDbTestTemplate implements NotificationListener {
	private List<AbstractFodsEvent> events;
	private DataSource ds;

	@Before
	public void registerMxBeanListener() throws InstanceNotFoundException, IOException {
		events = new ArrayList<AbstractFodsEvent>();
		ds = getFoDS();
		ManagementFactory.getPlatformMBeanServer().addNotificationListener(configuration.getMxBeanObjectName(AbstractFoDataSourceFactory.FODS_JMX_SUFIX), this, null, null);
	}



	@After
	public void unregisterMxBeanListener() throws InstanceNotFoundException, ListenerNotFoundException {
		ManagementFactory.getPlatformMBeanServer().removeNotificationListener(configuration.getMxBeanObjectName(AbstractFoDataSourceFactory.FODS_JMX_SUFIX), this);
		//		System.out.println(events);
	}



	//	@Test public void testWaitForever() throws InterruptedException {	Thread.sleep(Long.MAX_VALUE); }

	@Test
	public void testSwitch() throws Exception {
		String n0 = getDbnameForConnection(ds.getConnection());
		stop(0);
		String n1 = getDbnameForConnection(ds.getConnection());
		Thread.sleep(50);
		printEvents();

		ActiveDatabaseChangedEvent ev1 = (ActiveDatabaseChangedEvent) events.get(0);
		Assert.assertNull(ev1.getFromDbName());
		Assert.assertEquals("db0", ev1.getToDbName());

		DatabaseFiledEvent ev2 = (DatabaseFiledEvent) events.get(1);
		Assert.assertEquals("db0", ev2.getDbName());

		DatabaseStatusChangedEvent ev3 = (DatabaseStatusChangedEvent) events.get(2);
		Assert.assertEquals("db0", ev3.getDbName());
		Assert.assertEquals(FodsDbStateStatus.VALID, ev3.getPrevStatus());
		Assert.assertEquals(FodsDbStateStatus.BROKEN, ev3.getNewStatus());

		ActiveDatabaseChangedEvent ev4 = (ActiveDatabaseChangedEvent) events.get(3);
		Assert.assertEquals("db0", ev4.getFromDbName());
		Assert.assertEquals("db1", ev4.getToDbName());

		Assert.assertEquals(4, events.size());
	}



	@Test
	public void testSwitchOutOfConnections() throws Exception {
		stop(0);
		stop(1);
		try {
			getDbnameForConnection(ds.getConnection());
		} catch (SQLException e) { /* ignore */ }
		Thread.sleep(500);

		DatabaseFiledEvent ev1 = (DatabaseFiledEvent) events.get(0);
		Assert.assertEquals("db0", ev1.getDbName());

		DatabaseStatusChangedEvent ev1b = (DatabaseStatusChangedEvent) events.get(1);
		Assert.assertEquals("db0", ev1b.getDbName());
		Assert.assertEquals(FodsDbStateStatus.VALID, ev1b.getPrevStatus());
		Assert.assertEquals(FodsDbStateStatus.BROKEN, ev1b.getNewStatus());

		DatabaseFiledEvent ev2 = (DatabaseFiledEvent) events.get(2);
		Assert.assertEquals("db1", ev2.getDbName());

		DatabaseStatusChangedEvent ev2b = (DatabaseStatusChangedEvent) events.get(3);
		Assert.assertEquals("db1", ev2b.getDbName());
		Assert.assertEquals(FodsDbStateStatus.VALID, ev2b.getPrevStatus());
		Assert.assertEquals(FodsDbStateStatus.BROKEN, ev2b.getNewStatus());

		Assert.assertTrue(events.get(4) instanceof NoMoreDatabasesEvent);
		Assert.assertEquals(5, events.size());
	}



	@Test
	public void testEventOnForceChange() throws InterruptedException {
		ObjectName objectName = configuration.getMxBeanObjectName(AbstractFoDataSourceFactory.FODS_JMX_SUFIX);
		FoDataSourceConsoleMXBean bean = JMX.newMXBeanProxy(ManagementFactory.getPlatformMBeanServer(), objectName, FoDataSourceConsoleMXBean.class);
		Assert.assertTrue(bean.forceSetCurrentDatabaseName("db1"));
		Thread.sleep(500);
		ActiveDatabaseChangedEvent ev1 = (ActiveDatabaseChangedEvent) events.get(0);
		Assert.assertNull(ev1.getFromDbName());
		Assert.assertEquals("db1", ev1.getToDbName());
	}



	public void handleNotification(Notification notification, Object handback) {
		if (notification instanceof EventNotification) {
			EventNotification eventNotification = (EventNotification) notification;
			events.add(eventNotification.getEvent());
		}
	}

	private void printEvents() {
		for(Object event : events) {
			System.out.println("DbEvent: " + event);
		}
	}

}
