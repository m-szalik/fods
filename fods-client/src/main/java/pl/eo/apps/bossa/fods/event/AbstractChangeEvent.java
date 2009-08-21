package pl.eo.apps.bossa.fods.event;

import java.io.Serializable;

public abstract class AbstractChangeEvent implements Serializable {
	private static final long serialVersionUID = 698999080750942836L;
	private int index;
	
	public AbstractChangeEvent(int index) {
		this.index = index;
	}
		
	public int getIndex() {
		return index;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(index="+index+")";
	}
		
}
