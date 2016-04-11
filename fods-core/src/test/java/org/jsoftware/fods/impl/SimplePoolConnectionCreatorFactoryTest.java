package org.jsoftware.fods.impl;

import org.jsoftware.fods.AbstractDbTestTemplate;
import org.jsoftware.fods.client.ext.ConnectionCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class SimplePoolConnectionCreatorFactoryTest extends AbstractDbTestTemplate {
	private ConnectionCreator cc;



	@Before
	public void before() throws Exception {
		cc = configuration.getDatabaseConfigurationByName("db0").getConnectionCreator();
		cc.start();
	}



	@After
	public void after() {
		cc.stop();
	}



	@Test(expected = SQLException.class)
	public void getConnectionOutOfConnectionsTest() throws Exception {
		for (int i = 0; i < 10; i++) {
			cc.getConnection();
		}
	}



	@Test
	public void getConnection1() throws SQLException {
		for (int i = 0; i < 10; i++) {
			Connection connection = cc.getConnection();
			connection.close();
		}
	}

}
