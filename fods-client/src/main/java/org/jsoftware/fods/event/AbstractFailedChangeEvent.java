package org.jsoftware.fods.event;

/**
 * Event on database changed.
 * @author szalik
 *
 */
public class AbstractFailedChangeEvent extends AbstractChangeEvent {
	private Throwable reason;
	
	public AbstractFailedChangeEvent(String dbname, Throwable reason) {
		super(dbname);
		this.reason = reason;
	}
	
	public Throwable getReason() {
		return reason;
	}

	private static final long serialVersionUID = 7135297855355440977L;

}
