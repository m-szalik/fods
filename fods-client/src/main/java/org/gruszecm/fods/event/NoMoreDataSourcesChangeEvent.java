package org.gruszecm.fods.event;

/**
 * 
 * @author szalik
 */
public class NoMoreDataSourcesChangeEvent extends AbstractChangeEvent {

	private static final long serialVersionUID = -3401080210630831351L;


	public NoMoreDataSourcesChangeEvent() {
		super(-1);
	}

	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
