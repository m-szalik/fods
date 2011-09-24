package org.jsoftware.fods.event;

import org.jsoftware.fods.client.ext.FodsDbState;

/**
 * On database status changed to {@link FodsDbState.STATE#BROKEN}
 * @author szalik
 */
public class DatabaseFiledEvent extends AbstractFodsEvent {

	private Throwable reason;
	
	
	public Throwable getReason() {
		return reason;
	}
	
	public DatabaseFiledEvent(String dbname, Throwable reason) {
		super(dbname);
		this.reason = reason;
	}

	private static final long serialVersionUID = -3850452655335720982L;

}
