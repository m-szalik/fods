package org.jsoftware.fods.impl.stats;

import java.io.Serializable;

import org.jsoftware.fods.jmx.JMXStatistics;

/**
 * FoDS statistic item.
 * <p>Statistics for particular database.</p>
 * @author szalik
 */
public class StatisticsItem implements Serializable {
	private static final long serialVersionUID = -2481298760422578331L;
	long get, release;
	int breakTimes;
	long executedQueries, executedSelectQueries;
	
	
	public void addBreak() {
		breakTimes++;
	}
	
	public void addRecovery() {
	}
		
	
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("get/release:").append(get);
		out.append('/').append(release);
		out.append(" (").append(get-release).append(")\n");
		out.append("breakTimes:").append(breakTimes).append('\n');
		return out.toString();
	}
	
	public JMXStatistics createJMXStatistics(String myDbName) {
		return new JMXStatistics(get, release, breakTimes, executedQueries, executedSelectQueries, myDbName);
	}
}
