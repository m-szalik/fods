package org.jsoftware.fods.stats;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * FoDS statistics.
 * @author szalik
 *
 */
public class Statistics {
	private Map<String, StatisticsItem> statisticItems;
	private long startTime;
	
	public Statistics(Collection<String> dbNames) {
		this.startTime = System.currentTimeMillis();
		
		this.statisticItems = new HashMap<String, StatisticsItem>();
		for(String name : dbNames) {
			statisticItems.put(name, new StatisticsItem());
		}
	}
	
	public StatisticsItem getItem(String name) {
		return statisticItems.get(name);
	}
	
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("Statistics - " + new Date(startTime)).append("\n:");
		for(String dbn : statisticItems.keySet()) {
			out.append(dbn).append(": ");
			out.append(statisticItems.get(dbn).toString());
			out.append('\n');
		}
		return out.toString();
	}

}
