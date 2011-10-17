package org.jsoftware.fods.tester.host;

import java.sql.SQLException;

import javax.sql.DataSource;

public interface TestScenerio {

	void init(TesterConfig tc, DataSource ds);
	
	void test() throws SQLException;
	
	void before() throws Exception;
	
	void after() throws Exception;
	
}
