package pl.eo.apps.bossa.fods.client;

import pl.eo.apps.bossa.fods.event.AbstractChangeEvent;

/**
 * 
 * @author szalik
 */
public interface ChangeEventListener {
	
	void onEvent(AbstractChangeEvent event);
	
}
