package org.jsoftware.fods.event;

import org.jsoftware.fods.client.ext.FodsDbStateStatus;


/**
 * If there are no more {@link FodsDbStateStatus#VALID} databases.
 * @author szalik
 */
public class NoMoreDatabasesEvent extends AbstractFodsEvent {

	private static final long serialVersionUID = -3401080210630831351L;


	public NoMoreDatabasesEvent() {
		super(null);
	}

	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
