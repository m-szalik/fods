package org.jsoftware.fods.event;


/**
 * 
 * @author szalik
 */
public class RecoverySucessEvent extends AbstractChangeEvent {

	public RecoverySucessEvent(String dbname) {
		super(dbname);
	}

	private static final long serialVersionUID = 1L;

}
