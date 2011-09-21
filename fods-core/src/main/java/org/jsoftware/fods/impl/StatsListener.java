package org.jsoftware.fods.impl;

import org.jsoftware.fods.client.ChangeEventListener;
import org.jsoftware.fods.event.AbstractChangeEvent;
import org.jsoftware.fods.event.DatabaseFiledChangeEvent;
import org.jsoftware.fods.event.RecoveryChangeEvent;
import org.jsoftware.fods.stats.Statistics;


public class StatsListener implements ChangeEventListener {
	private Statistics statistics;
	
	public StatsListener(Statistics stats) {
		this.statistics = stats;
	}

	public void onEvent(AbstractChangeEvent event) {
		if (event instanceof DatabaseFiledChangeEvent) {
			statistics.getItem(event.getIndex()).addBreak();
		}
		if (event instanceof RecoveryChangeEvent) {
			statistics.getItem(event.getIndex()).addRecovery();
		}
	}

}
