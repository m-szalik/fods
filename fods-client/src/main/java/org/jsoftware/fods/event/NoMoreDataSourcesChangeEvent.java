package org.jsoftware.fods.event;


/**
 * If there are no more {@link FodsDbState.}
 * @author szalik
 */
public class NoMoreDataSourcesChangeEvent extends AbstractFodsEvent {

	private static final long serialVersionUID = -3401080210630831351L;


	public NoMoreDataSourcesChangeEvent() {
		super(null);
	}

	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
