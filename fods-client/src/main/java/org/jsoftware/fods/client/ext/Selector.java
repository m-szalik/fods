package org.jsoftware.fods.client.ext;



/**
 * 
 * Database selection strategy.
 * @author szalik
 */
public interface Selector {

	String select(FodsState databaseState);

}
