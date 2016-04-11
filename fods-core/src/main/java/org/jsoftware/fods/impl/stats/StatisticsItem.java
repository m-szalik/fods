package org.jsoftware.fods.impl.stats;

import org.jsoftware.fods.jmx.JMXStatistics;

import java.io.Serializable;

/**
 * FoDS statistic item.
 * <p>
 * Statistics for particular database.
 * </p>
 * @author szalik
 */
public class StatisticsItem implements Serializable {
	private static final long serialVersionUID = -2481298760422578331L;
	long get, release;
	int breakTimes, recoveryTimes;
	long executedQueries, executedSelectQueries;
	long executedSelectTime, executedTime;



	public void addBreak() {
		breakTimes++;
	}



	public void addRecovery() {
		recoveryTimes++;
	}



	public JMXStatistics createJMXStatistics(String myDbName) {
		double avrSelectQyeryMS = (executedSelectQueries == 0) ? 0 : (double)executedSelectTime / (double)executedSelectQueries;
		double avrQyeryMS = (executedQueries == 0) ? 0 : (double) executedTime / (double) executedQueries;
		return new JMXStatistics(get, release, breakTimes, recoveryTimes, executedQueries, executedSelectQueries, myDbName, avrSelectQyeryMS, avrQyeryMS);
	}
}
