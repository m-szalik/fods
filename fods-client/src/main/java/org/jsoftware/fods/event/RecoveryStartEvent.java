package org.jsoftware.fods.event;

/**
 * On database recovery try.
 * @see RecoverySuccessEvent
 * @see RecoveryFailedEvent
 * @author szalik
 */
public class RecoveryStartEvent extends AbstractFodsEvent {

	public RecoveryStartEvent(String dbname) {
		super(dbname);
	}

	private static final long serialVersionUID = 1L;

}
