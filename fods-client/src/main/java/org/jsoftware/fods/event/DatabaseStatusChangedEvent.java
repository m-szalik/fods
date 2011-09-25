package org.jsoftware.fods.event;

import org.jsoftware.fods.client.ext.FodsDbStateStatus;
import org.jsoftware.fods.jmx.FoDataSourceConsoleMXBean;

/**
 * Invoked on database {@link FodsDbStateStatus} changed.
 * TODO add this event on {@link FoDataSourceConsoleMXBean#forceSetCurrentDatabaseName(String)} 
 * @author szalik
 */
public class DatabaseStatusChangedEvent extends AbstractFodsEvent {
	
	private FodsDbStateStatus prevStatus;
	private FodsDbStateStatus newStatus;

	public DatabaseStatusChangedEvent(String dbName, FodsDbStateStatus from, FodsDbStateStatus to) {
		super(dbName);
		this.prevStatus = from;
		this.newStatus = to;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(dbName="+getDbName()+" " + prevStatus + " --> " + newStatus + ")";
	}
	
	public FodsDbStateStatus getNewStatus() {
		return newStatus;
	}
	
	public FodsDbStateStatus getPrevStatus() {
		return prevStatus;
	}

	private static final long serialVersionUID = -5168363946234877628L;

}
