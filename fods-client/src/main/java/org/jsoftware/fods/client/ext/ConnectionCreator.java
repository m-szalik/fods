package org.jsoftware.fods.client.ext;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionCreator {

	Connection getConnection() throws SQLException;
	
}
