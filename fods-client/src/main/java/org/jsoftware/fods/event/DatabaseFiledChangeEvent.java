package org.jsoftware.fods.event;

public class DatabaseFiledChangeEvent extends AbstractFailedChangeEvent {

	public DatabaseFiledChangeEvent(String dbname, Throwable reason) {
		super(dbname, reason);
	}

	private static final long serialVersionUID = -3850452655335720982L;

}
