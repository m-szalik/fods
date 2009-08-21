package pl.eo.apps.bossa.fods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pl.eo.apps.bossa.fods.client.ChangeEventListener;
import pl.eo.apps.bossa.fods.event.AbstractChangeEvent;
import pl.eo.apps.bossa.fods.log.LogLevel;
import pl.eo.apps.bossa.fods.log.Logger;

/**
 * 
 * @author szalik
 */
public class ChangeEventsThread extends Thread {
	private List<ChangeEventListener> chaneEventListeners;
	private List<AbstractChangeEvent> waitingEvents;
	private Logger logger;
	
	public ChangeEventsThread(Logger logger) {
		super();
		setName("waitingEventsSender");
		setDaemon(true);
	
		this.logger = logger;
		this.chaneEventListeners = Collections.emptyList();
		this.waitingEvents = new ArrayList<AbstractChangeEvent>();
	}
	
	
	@Override
	public void run() {
		List<AbstractChangeEvent> eventsToSend = new LinkedList<AbstractChangeEvent>(); 
		while (true) {
			synchronized (waitingEvents) {
				if (waitingEvents.isEmpty()) try {	waitingEvents.wait(); } catch (InterruptedException e) { }
				eventsToSend.addAll(waitingEvents);
				waitingEvents.clear();
			}
			for (Iterator<AbstractChangeEvent> it = eventsToSend.iterator(); it.hasNext();) {
				AbstractChangeEvent e = it.next();
				it.remove();
				for(ChangeEventListener l : chaneEventListeners) {
					try {
						l.onEvent(e);
					} catch (Exception ex) {
						logger.log(LogLevel.CRITICAL, "changeEventListener - " + l.getClass().getName() + " throws " + ex, ex);
					}
				}
			}
		} // while(true)
	}


	public void addChangeEventListener(ChangeEventListener listener) {
		if (listener != null) {
			List<ChangeEventListener> l = new LinkedList<ChangeEventListener>();
			l.addAll(chaneEventListeners);
			l.add(listener);
			chaneEventListeners = l;
		}
	}


	public void notifyChangeEvent(AbstractChangeEvent event) {
		synchronized (waitingEvents) {
			waitingEvents.add(event);
			waitingEvents.notify();
		}
	}
	
}
