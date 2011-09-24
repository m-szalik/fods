package org.jsoftware.fods.event;

import java.io.Serializable;

/**
 * 
 * @author szalik
 *
 */
public abstract class AbstractChangeEvent implements Serializable {
	private static final long serialVersionUID = 698999080750942836L;
	private String dbname;
	
	public AbstractChangeEvent(String dbname) {
		this.dbname = dbname;
	}
		
	public String getDbname() {
		return dbname;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(dbname="+dbname+")";
	}
		
}
