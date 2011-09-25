package org.jsoftware.fods.event;

/**
 * Invoked on active database changed. 
 * TODO add this event on {@link FoDataSourceConsoleMXBean#forceSetCurrentDatabaseName(String)}
 * @author szalik
 */
public class ActiveDatabaseChangedEvent extends AbstractFodsEvent {
	private String from;

	public ActiveDatabaseChangedEvent(String toDbName, String fromDbName) {
		super(toDbName);
		this.from = fromDbName;
	}

	public String getFromDbName() {
		return from;
	}

	public String getToDbName() {
		return getDbName();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + from + " --> " + getDbName() + ")";
	}

	private static final long serialVersionUID = -5168363946234877658L;

}
