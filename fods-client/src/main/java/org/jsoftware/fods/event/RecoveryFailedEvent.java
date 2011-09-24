package org.jsoftware.fods.event;

public class RecoveryFailedEvent extends AbstractChangeEvent {

	private static final long serialVersionUID = 7725076007739610536L;

	public RecoveryFailedEvent(String dbname, Exception reason) {
		super(dbname);
	}

}
