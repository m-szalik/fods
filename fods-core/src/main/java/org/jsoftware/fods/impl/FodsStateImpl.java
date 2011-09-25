package org.jsoftware.fods.impl;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jsoftware.fods.client.ext.FodsState;

/**
 * FoDS state object.
 * @author szalik
 */
public class FodsStateImpl implements Serializable, FodsState {
	private static final long serialVersionUID = -1497442471039822107L;
	private Map<String,FodsDbStateImpl> dbstates;
	private String currentDatabase;
	
	public FodsStateImpl(Collection<String> names) {
		dbstates = new HashMap<String, FodsDbStateImpl>();
		for(String name : names) {
			dbstates.put(name, new FodsDbStateImpl());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jsoftware.fods.client.ext.FodsState#getDbstate(java.lang.String)
	 */
	public FodsDbStateImpl getDbstate(String name) {
		return dbstates.get(name);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jsoftware.fods.client.ext.FodsState#getCurrentDatabase()
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
