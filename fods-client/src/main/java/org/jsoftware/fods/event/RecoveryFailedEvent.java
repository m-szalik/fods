package org.jsoftware.fods.event;

/**
 * On database recovery failed.
 * @see RecoveryStartEvent
 * @see RecoverySucessEvent
 * @author szalik
 */
public class RecoveryFailedEvent extends AbstractFodsEvent {
	private Throwable reason;
	private static final long serialVersionUID = 7725076007739610536L;

	public RecoveryFailedEvent(String dbname, Exception reason) {
		super(dbname);
		this.reason = reason;
	}
	
	public Throwable getReason() {
		return reason;
	}

}
