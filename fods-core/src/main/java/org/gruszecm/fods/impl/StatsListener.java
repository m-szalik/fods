package org.gruszecm.fods.impl;

import org.gruszecm.fods.client.ChangeEventListener;
import org.gruszecm.fods.event.AbstractChangeEvent;
import org.gruszecm.fods.event.DatabaseFiledChangeEvent;
import org.gruszecm.fods.event.RecoveryChangeEvent;
import org.gruszecm.fods.stats.Statistics;


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
