package org.jsoftware.fods.event;

import org.jsoftware.fods.jmx.FODataSourceConsoleMBean;

/**
 * Invoked on active database changed.
 * TODO add this event on {@link FODataSourceConsoleMBean#forceSetCurrentDatabaseName(String)} 
 * @author szalik
 */
public class DatabaseChangedChangeEvent extends AbstractFodsEvent {
	private String from;
	private boolean userRequest;
	
	public DatabaseChangedChangeEvent(String toDbName, String fromDbName) {
		super(toDbName);
		this.from = fromDbName;
	}
	
	public boolean isUserRequest() {
		return userRequest;
	}
	
	public void setUserRequest(boolean userRequest) {
		this.userRequest = userRequest;
	}
	
	public String getFromDbName() {
		return from;
	}
	
	public String getToDbName() {
		return getDbname();
	}
		
	
	@Override
	public String toString() {
		String ur = userRequest ? ",userRequest" : "";
		return getClass().getSimpleName() + "(index="+from+" to " + getDbname() + ")" + ur;
	}

	private static final long serialVersionUID = -5168363946234877658L;

}
