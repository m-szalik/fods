package pl.eo.apps.bossa.fods.impl;

import pl.eo.apps.bossa.fods.client.ChangeEventListener;
import pl.eo.apps.bossa.fods.event.AbstractChangeEvent;
import pl.eo.apps.bossa.fods.event.DatabaseFiledChangeEvent;
import pl.eo.apps.bossa.fods.event.RecoveryChangeEvent;
import pl.eo.apps.bossa.fods.stats.Statistics;

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
