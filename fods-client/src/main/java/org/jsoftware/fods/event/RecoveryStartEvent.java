package org.jsoftware.fods.event;


/**
 * 
 * @author szalik
 */
public class RecoveryStartEvent extends AbstractChangeEvent {

	public RecoveryStartEvent(String dbname) {
		super(dbname);
	}

	private static final long serialVersionUID = 1L;

}
