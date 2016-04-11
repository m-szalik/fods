package org.jsoftware.fods.tester.host;

import javax.sql.DataSource;
import java.sql.SQLException;

public interface TestScenerio {

	void init(TesterConfig tc, DataSource ds);
	
	void test() throws SQLException;
	
	void before() throws Exception;
	
	void after() throws Exception;
	
}
