package org.gruszecm.fods.stats;

import java.util.Date;


public class Statistics {
	private StatisticsItem[] statisticsItems;
	private long startTime;
	
	public Statistics(int size) {
		this.startTime = System.currentTimeMillis();
		this.statisticsItems = new StatisticsItem[size];
		for(int i=0; i<statisticsItems.length; i++) {
			statisticsItems[i] = new StatisticsItem();
		}
	}
	
	public StatisticsItem getItem(int index) {
		return statisticsItems[index];
	}
	
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append("Statistics - " + new Date(startTime)).append("\n:");
		for(int i=0; i<statisticsItems.length; i++) {
			out.append(i).append(": ");
			out.append(statisticsItems[i].toString());
			out.append('\n');
		}
		return out.toString();
	}

}
