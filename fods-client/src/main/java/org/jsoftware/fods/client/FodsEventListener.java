package org.jsoftware.fods.client;

import org.jsoftware.fods.event.AbstractFodsEvent;

/**
 * Listener of {@link AbstractFodsEvent}
 * @author szalik
 */
public interface FodsEventListener {
	
	void onEvent(AbstractFodsEvent event);
	
}
