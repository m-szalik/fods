package pl.eo.apps.bossa.fods.impl;

import pl.eo.apps.bossa.fods.client.ext.DatabasesState;
import pl.eo.apps.bossa.fods.client.ext.Selector;

public abstract class AbstractSelector implements Selector {
	protected int currentIndex;
	protected DatabasesState state;
	
	public void forceSetIndex(int index) throws UnsupportedOperationException {
		this.currentIndex = index;
	}

	public void init(DatabasesState state) {
		this.state = state;
	}

	public int select() {
		return currentIndex;
	}

}
