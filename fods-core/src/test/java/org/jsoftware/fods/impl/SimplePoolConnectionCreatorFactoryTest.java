package org.jsoftware.fods.impl;

import java.sql.Connection;
import java.sql.SQLException;

import org.jsoftware.fods.AbstractDbTestTemplate;
import org.jsoftware.fods.client.ext.ConnectionCreator;
import org.junit.Ignore;
import org.junit.Test;


public class SimplePoolConnectionCreatorFactoryTest extends AbstractDbTestTemplate {

	@Test(expected=SQLException.class) @Ignore
	public void getConnectionOutOfConnectionsTest() throws SQLException {
		ConnectionCreator cc = configuration.getDatabaseConfigurationByName("db0").getConnectionCreator();
		for(int i=0; i<10; i++) {
			cc.getConnection();
		}
	}
	
	@Test
	public void getConnection1() throws SQLException {
		ConnectionCreator cc = configuration.getDatabaseConfigurationByName("db0").getConnectionCreator();
		for(int i=0; i<10; i++) {
			Connection connection = cc.getConnection();
			connection.close();
		}
	}
	
}
