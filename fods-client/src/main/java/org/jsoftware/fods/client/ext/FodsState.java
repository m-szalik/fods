package org.jsoftware.fods.client.ext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FodsState {
	private Map<String,FodsDbState> dbstates;
	private String currentDatabase;
	
	public FodsState(Collection<String> names) {
		dbstates = new HashMap<String, FodsDbState>();
		for(String name : names) {
			dbstates.put(name, new FodsDbState());
		}
	}
	
	public FodsDbState getDbstate(String name) {
		return dbstates.get(name);
	}
	
	public String getCurrentDatabase() {
		return currentDatabase;
	}
	
	public void setCurrentDatabase(String currentDatabase) {
		this.currentDatabase = currentDatabase;
	}
	
	
}
