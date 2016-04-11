package org.jsoftware.fods.tester.host.scenerious;

import org.jsoftware.fods.tester.host.AbstractScenerio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SingleInsertScenerio extends AbstractScenerio {

	@Override
	public void test() throws SQLException {
		Connection connection = dataSource.getConnection();
		PreparedStatement ps = connection.prepareStatement("INSERT INTO single_insert_scenerio (xval) VALUES (14)");
		ps.execute();
		ps.close();
		connection.close();
	}

}
