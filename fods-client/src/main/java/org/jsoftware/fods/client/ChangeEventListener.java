package org.jsoftware.fods.client;

import org.jsoftware.fods.event.AbstractChangeEvent;

/**
 * 
 * @author szalik
 */
public interface ChangeEventListener {
	
	void onEvent(AbstractChangeEvent event);
	
}
