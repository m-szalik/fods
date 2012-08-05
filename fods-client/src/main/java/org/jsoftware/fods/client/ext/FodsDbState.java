package org.jsoftware.fods.client.ext;

import java.sql.Connection;

/**
 * Single database state
 * @author szalik
 */
public interface FodsDbState {

	/**
	 * @return current database {@link FodsDbStateStatus}
	 */
	FodsDbStateStatus getStatus();



	/**
	 * @return number of milliseconds when last database error occurred.
	 */
	long getBrokenTime();



	/**
	 * If {@link Connection} to this database is read only.
	 * @return <tt>true</tt> if read only.
	 */
	boolean isReadOnly();

}
