package org.jsoftware.fods.jmx;

import java.beans.ConstructorProperties;
import java.io.Serializable;

public class JMXFodsDbState implements Serializable {
	private String dbName;
	private Long breakdownTS;
	private String lastBreakdownReason;
	private String statusAsString;
	
	@ConstructorProperties({"dbName", "status", "lastBreakdownReason", "breakdownTS"})
	public JMXFodsDbState(String dbName, String statusAsString, String reason, Long breakdownTS) {
		super();
		this.breakdownTS = breakdownTS;
		this.dbName = dbName;
		this.lastBreakdownReason = reason;
		this.statusAsString = statusAsString;
	}
	
	
	public JMXFodsDbState(String dbName, String statusAsString, Throwable reason, Long breakdownTS) {
		super();
		this.dbName = dbName;
		this.statusAsString = statusAsString;
		this.breakdownTS = breakdownTS;
		if (reason != null) {
			lastBreakdownReason = reason.toString();
		}
	}

	
	public String getDbName() {
		return dbName;
	}

	public Long getBreakdownTS() {
		return breakdownTS;
	}

	public String getLastBreakdownReason() {
		return lastBreakdownReason;
	}

	public String getStatus() {
		return statusAsString;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
