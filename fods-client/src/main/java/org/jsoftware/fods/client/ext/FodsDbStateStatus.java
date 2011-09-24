package org.jsoftware.fods.client.ext;

import org.jsoftware.fods.jmx.FODataSourceConsoleMBean;
/**
 * @see FodsDbState#getState()
 * @author szalik
 */
public enum FodsDbStateStatus {
	
	/** Database is valid and working */
		VALID, 
		/** Database turnned off by fods administrator 
		 * @see FODataSourceConsoleMBean
		 */
		DISCONNETED, 
		/**
		 * Database broken
		 */
		BROKEN
}