package org.jsoftware.fods.integration;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.jsoftware.fods.AbstractDbTestTemplate;
import org.junit.Assert;
import org.junit.Test;

public class FoDSTest extends AbstractDbTestTemplate {

	@Test
	public void testSwitch() throws Exception {
		DataSource ds = getFoDS();
		Assert.assertEquals("db0", getDbnameForConnection(ds.getConnection()));
		stop(0);
		Assert.assertEquals("db1", getDbnameForConnection(ds.getConnection()));
	}


	@Test
	public void testSwitchAround() throws Exception {
		DataSource ds = getFoDS();
		Assert.assertEquals("db0", getDbnameForConnection(ds.getConnection()));
		stop(0);
		Assert.assertEquals("db1", getDbnameForConnection(ds.getConnection()));
		stop(1);
		start(0);
		Thread.sleep(2000);
		Assert.assertEquals("db0", getDbnameForConnection(ds.getConnection()));
	}

	@Test(expected=SQLException.class)
	public void testSwitchOutOfConnections() throws Exception {
		DataSource ds = getFoDS();
		stop(0);
		stop(1);
		getDbnameForConnection(ds.getConnection());
	}

}
