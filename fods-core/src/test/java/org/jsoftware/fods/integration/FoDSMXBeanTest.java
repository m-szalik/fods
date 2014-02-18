package org.jsoftware.fods.integration;

import org.jsoftware.fods.AbstractDbTestTemplate;
import org.jsoftware.fods.client.ext.FodsDbStateStatus;
import org.jsoftware.fods.impl.AbstractFoDataSourceFactory;
import org.jsoftware.fods.jmx.FoDataSourceConsoleMXBean;
import org.jsoftware.fods.jmx.JMXFodsDbState;
import org.jsoftware.fods.jmx.JMXStatistics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public class FoDSMXBeanTest extends AbstractDbTestTemplate {
	private DataSource ds;
	private FoDataSourceConsoleMXBean bean;



	@Before
	public void registerMxBeanListener() throws InstanceNotFoundException, IOException {
		ds = getFoDS();
		ObjectName objectName = configuration.getMxBeanObjectName(AbstractFoDataSourceFactory.FODS_JMX_SUFIX);
		bean = JMX.newMXBeanProxy(ManagementFactory.getPlatformMBeanServer(), objectName, FoDataSourceConsoleMXBean.class);
	}



	@Test
	public void fodsNameTest() throws Exception {
		Assert.assertEquals("testDS", bean.getFodsName());
	}



	@Test
	public void currentDatabaseNameTest() throws SQLException {
		Assert.assertEquals("db0", getDbnameForConnection(ds.getConnection()));
		Assert.assertEquals("db0", bean.getCurrentDatabaseName());
		stop(0);
		Assert.assertEquals("db1", getDbnameForConnection(ds.getConnection()));
		Assert.assertEquals("db1", bean.getCurrentDatabaseName());
	}



	@Test
    @Ignore // FIXME
	public void databaseStateTest() throws SQLException {
		stop(0);
		Assert.assertEquals("db1", getDbnameForConnection(ds.getConnection()));
		JMXFodsDbState state = bean.getCurrentDatabaseState("db0");
		Assert.assertEquals(FodsDbStateStatus.BROKEN.name(), state.getStatus());
		Assert.assertTrue(state.getBreakdownTS() > 0);
		Assert.assertNotNull(state.getLastBreakdownReason());

		state = bean.getCurrentDatabaseState("db1");
		Assert.assertEquals(FodsDbStateStatus.VALID.name(), state.getStatus());
		Assert.assertNull(state.getBreakdownTS());
		Assert.assertNull(state.getLastBreakdownReason());
	}



	@Test
	public void databaseTurnOff1() throws SQLException {
		Assert.assertEquals("db0", getDbnameForConnection(ds.getConnection()));
		Assert.assertTrue(bean.turnOffDatabase("db0"));
		Assert.assertEquals("db1", getDbnameForConnection(ds.getConnection()));
	}



	@Test
	public void databaseTurnOff2() throws SQLException {
		Assert.assertEquals(FodsDbStateStatus.VALID.name(), bean.getCurrentDatabaseState("db0").getStatus());
		Assert.assertTrue(bean.turnOffDatabase("db0"));
		Assert.assertEquals(FodsDbStateStatus.DISCONNETED.name(), bean.getCurrentDatabaseState("db0").getStatus());
		Assert.assertFalse(bean.turnOffDatabase("db0"));
		Assert.assertEquals(FodsDbStateStatus.DISCONNETED.name(), bean.getCurrentDatabaseState("db0").getStatus());
		Assert.assertTrue(bean.turnOnDatabase("db0"));
		Assert.assertEquals(FodsDbStateStatus.VALID.name(), bean.getCurrentDatabaseState("db0").getStatus());
	}



	@Test
	public void databaseTestTest() throws SQLException {
		stop(1);
		Assert.assertTrue(bean.test("db0"));
		Assert.assertFalse(bean.test("db1"));
	}



	@Test(expected = NoSuchElementException.class)
	public void databaseTestInvalidNameTest() {
		Assert.assertNull(bean.test("invalidName"));
	}



	@Test
	public void databaseTestReportTest() {
		Assert.assertNotNull(bean.testRaport());
	}



	@Test
	public void databaseStatisticsTest1() throws SQLException {
		Connection con = ds.getConnection();
		Assert.assertEquals("db0", getDbnameForConnection(con));
		con.close();
		JMXStatistics db0s = null;
		for (JMXStatistics st : bean.getStatistics()) {
			if (st.getDbName().equals("db0")) {
				db0s = st;
			}
		}

		Assert.assertNotNull(db0s);
		Assert.assertEquals(1, db0s.getGet());
		Assert.assertEquals(1, db0s.getRelease());
		Assert.assertEquals(0, db0s.getBreakTimes());
	}

}
