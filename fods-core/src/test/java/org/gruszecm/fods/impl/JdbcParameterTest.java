package org.gruszecm.fods.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class JdbcParameterTest {

	@Test
	public void testParseMySqlLoadbalance() {
		String jdbc = "jdbc:mysql:loadbalance://db-02.com,db-01.com/database01";
		JdbcParameter[] jps = JdbcParameter.parse(jdbc);
		assertEquals("jdbc:mysql://db-02.com/database01", jps[0].getUrl());
		assertEquals("jdbc:mysql://db-01.com/database01", jps[1].getUrl());
		assertEquals("db-02.com", jps[0].getDbId());
		assertEquals("db-01.com", jps[1].getDbId());
		assertEquals(2, jps.length);
	}

}
