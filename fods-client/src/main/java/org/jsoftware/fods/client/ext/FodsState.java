package org.jsoftware.fods.client.ext;

/**
 * FoDS state.
 * @author szalik
 */
public interface FodsState {

	FodsDbState getDbstate(String name);



	String getCurrentDatabase();
}
