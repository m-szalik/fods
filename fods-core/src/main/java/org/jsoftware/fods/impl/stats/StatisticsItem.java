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
	private long get, release;
	private int breakTimes;
	
	public void addBreak() {
		breakTimes++;
	}
	
	public void addRecovery() {
	}
	
	public void addGet() {
		get++;
	}
	
	public void addRelease() {
		release++;
	}
	
	public int getBreakTimes() {
		return breakTimes;
	}
	
	public long getGet() {
		return get;
	}
	
	public long getRelease() {
		return release;
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
		return new JMXStatistics(get, release, breakTimes, myDbName);
	}
}
