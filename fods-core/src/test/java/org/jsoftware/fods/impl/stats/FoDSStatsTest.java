package org.jsoftware.fods.impl.stats;

import org.jsoftware.fods.AbstractDbTestTemplate;
import org.jsoftware.fods.impl.FoDataSourceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.management.InstanceNotFoundException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FoDSStatsTest extends AbstractDbTestTemplate {
	private DataSource ds;
	private StatisticsItem db0si;



	@Before
	public void registerMxBeanListener() throws InstanceNotFoundException, IOException {
		FoDataSourceImpl fods = (FoDataSourceImpl) getFoDS();
		ds = fods;
		db0si = fods.getStatistics().getItem("db0");
	}



	@Test
	public void releaseStatTest() throws SQLException {
		Connection con = ds.getConnection();
		Assert.assertEquals(1, db0si.get);
		Assert.assertEquals(0, db0si.release);
		con.close();
		Assert.assertEquals(1, db0si.release);
	}



	@Test
	public void statementStatTest() throws SQLException {
		Connection con = ds.getConnection();
		PreparedStatement ps = con.prepareStatement("SELECT * FROM stable");
		ps.execute();
		ps.executeQuery();
		ps.close();
		con.close();
		Assert.assertEquals(1, db0si.release);
	}

}
