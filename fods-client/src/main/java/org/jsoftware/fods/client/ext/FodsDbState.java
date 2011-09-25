package org.jsoftware.fods.client.ext;


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
}
