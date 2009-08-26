package org.gruszecm.fods.impl;


public class DefaultSelector extends AbstractSelector  {

	public void onStateChanged() {
		for(int i=0; i<state.size(); i++) {
			if (! state.isBroken(i)) {
				currentIndex = i;
				break;
			}
		}
	}

}
