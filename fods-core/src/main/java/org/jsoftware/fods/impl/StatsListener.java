package org.jsoftware.fods.impl;

import org.jsoftware.fods.client.ChangeEventListener;
import org.jsoftware.fods.event.AbstractChangeEvent;
import org.jsoftware.fods.event.DatabaseFiledChangeEvent;
import org.jsoftware.fods.event.RecoverySucessEvent;
import org.jsoftware.fods.stats.Statistics;


public class StatsListener implements ChangeEventListener {
	private Statistics statistics;
	
	public StatsListener(Statistics stats) {
		this.statistics = stats;
	}

	public void onEvent(AbstractChangeEvent event) {
		if (event instanceof DatabaseFiledChangeEvent) {
			statistics.getItem(event.getDbname()).addBreak();
		}
		if (event instanceof RecoverySucessEvent) {
			statistics.getItem(event.getDbname()).addRecovery();
		}
	}

}
