package org.jsoftware.fods.client.ext;

import org.jsoftware.fods.jmx.FoDataSourceConsoleMXBean;


/**
 * @author szalik
 */
public enum FodsDbStateStatus {
	
	/** Database is valid and working */
		VALID, 
		/** Database turnned off by fods administrator 
		 * @see FoDataSourceConsoleMXBean
		 */
		DISCONNETED, 
		/**
		 * Database broken
		 */
		BROKEN
}