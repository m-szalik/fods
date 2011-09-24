package org.jsoftware.fods.event;

/**
 * 
 * @author szalik
 */
public class NoMoreDataSourcesChangeEvent extends AbstractChangeEvent {

	private static final long serialVersionUID = -3401080210630831351L;


	public NoMoreDataSourcesChangeEvent() {
		super(null);
	}

	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
