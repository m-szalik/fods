package org.jsoftware.fods.impl;

import org.jsoftware.fods.client.ext.DatabasesState;
import org.jsoftware.fods.client.ext.Selector;

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
