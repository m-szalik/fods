package org.jsoftware.fods.jmx;

import java.beans.ConstructorProperties;
import java.io.Serializable;

public class JMXStatistics implements Serializable {
	private long get, release;
	private int breakTimes;
	private String dbName;

	@ConstructorProperties({ "get", "release", "breakTimes", "dbName" })
	public JMXStatistics(long get, long release, int breakTimes, String dbName) {
		super();
		this.get = get;
		this.release = release;
		this.breakTimes = breakTimes;
		this.dbName = dbName;
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

	/**
	 * 
	 */
	private static final long serialVersionUID = -1585112927126973734L;

}
