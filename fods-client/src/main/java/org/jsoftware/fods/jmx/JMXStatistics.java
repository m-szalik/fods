package org.jsoftware.fods.jmx;

import java.beans.ConstructorProperties;
import java.io.Serializable;

public class JMXStatistics implements Serializable {
	private long get, release;
	private int breakTimes, recoveryTimes;
	private String dbName;
	private long executedQueries, executedSelectQueries;
	private double avrSelectQueryTimeMs, avrQueryTimeMs;

	@ConstructorProperties({ "get", "release", "breakTimes", "recoveryTimes", "executedQueries", "executedSelectQueries", "dbName", "avrSelectQueryTimeMs", "avrQueryTimeMs"})
	public JMXStatistics(long get, long release, int breakTimes, int recoveryTimes, long executedQueries, long executedSelectQueries, String dbName, double avrSelectQyeryMS, double avrQyeryMS) {
		super();
		this.get = get;
		this.release = release;
		this.breakTimes = breakTimes;
		this.dbName = dbName;
		this.avrQueryTimeMs = avrQyeryMS;
		this.avrSelectQueryTimeMs = avrSelectQyeryMS;
	}
	
	public double getAvrQueryTimeMs() {
		return avrQueryTimeMs;
	}
	
	public double getAvrSelectQueryTimeMs() {
		return avrSelectQueryTimeMs;
	}
	
	public int getRecoveryTimes() {
		return recoveryTimes;
	}

	public long getGet() {
		return get;
	}

	public long getRelease() {
		return release;
	}

	public int getBreakTimes() {
		return breakTimes;
	}

	public String getDbName() {
		return dbName;
	}
	
	public long getExecutedQueries() {
		return executedQueries;
	}
	
	public long getExecutedSelectQueries() {
		return executedSelectQueries;
	}

	private static final long serialVersionUID = -1585112927126973734L;

}
