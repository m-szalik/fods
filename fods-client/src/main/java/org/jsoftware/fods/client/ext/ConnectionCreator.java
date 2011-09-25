package org.jsoftware.fods.client.ext;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@link ConnectionCreator} is a factory for {@link Connection} used by fods to create new {@link Connection}s.
 * @see ManageableViaMXBean
 * @author szalik
 */
public interface ConnectionCreator {

	Connection getConnection() throws SQLException;
	
}
