package org.jsoftware.fods.client.ext;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * FoDS state object.
 * @author szalik
 */
public class FodsState {
	private Map<String,FodsDbState> dbstates;
	private String currentDatabase;
	
	public FodsState(Collection<String> names) {
		dbstates = new HashMap<String, FodsDbState>();
		for(String name : names) {
			dbstates.put(name, new FodsDbState());
		}
	}
	
	/**
	 * @param name
	 * @return {@link FodsDbState} for particular database.
	 */
	public FodsDbState getDbstate(String name) {
		return dbstates.get(name);
	}
	
	/**
	 * @return last connected database name
	 */
	public String getCurrentDatabase() {
		return currentDatabase;
	}
	
	/**
	 * Set database that should be used on next {@link Connection} request.
	 * @param currentDatabase
	 */
	public void setCurrentDatabase(String currentDatabase) {
		this.currentDatabase = currentDatabase;
	}
	
	
}
