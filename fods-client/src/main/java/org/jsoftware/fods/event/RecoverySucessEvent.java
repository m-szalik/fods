package org.jsoftware.fods.event;

/**
 * On recovery database successful.
 * @see RecoveryFailedEvent
 * @see RecoveryStartEvent
 * @author szalik
 */
public class RecoverySucessEvent extends AbstractFodsEvent {

	public RecoverySucessEvent(String dbname) {
		super(dbname);
	}

	private static final long serialVersionUID = 1L;

}
