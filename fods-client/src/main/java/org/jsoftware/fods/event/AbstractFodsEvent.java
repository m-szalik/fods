package org.jsoftware.fods.event;

import java.io.Serializable;

/**
 * Super class for all fods events.
 * @author szalik
 */
public abstract class AbstractFodsEvent implements Serializable {
	private static final long serialVersionUID = 698999080750942836L;
	private String dbname;
	
	public AbstractFodsEvent(String dbname) {
		this.dbname = dbname;
	}
		
	public String getDbName() {
		return dbname;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(dbname="+dbname+")";
	}
		
}
