package org.jsoftware.fods.impl;

import org.jsoftware.fods.client.ext.Selector;


/**
 * Default database {@link Selector}
 * @author gruszecm
 *
 */
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
