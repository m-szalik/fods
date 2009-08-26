package org.gruszecm.fods.client;

import org.gruszecm.fods.event.AbstractChangeEvent;

/**
 * 
 * @author szalik
 */
public interface ChangeEventListener {
	
	void onEvent(AbstractChangeEvent event);
	
}
