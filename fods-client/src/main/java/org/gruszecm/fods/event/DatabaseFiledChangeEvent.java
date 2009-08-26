package org.gruszecm.fods.event;

public class DatabaseFiledChangeEvent extends AbstractFailedChangeEvent {

	public DatabaseFiledChangeEvent(int index, Throwable reason) {
		super(index, reason);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3850452655335720982L;

}
