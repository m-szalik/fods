package org.jsoftware.fods.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jsoftware.fods.client.FodsEventListener;
import org.jsoftware.fods.client.ext.LogLevel;
import org.jsoftware.fods.client.ext.Logger;
import org.jsoftware.fods.event.AbstractFodsEvent;

/**
 * This {@link Thread} sends notifications to all {@link FodsEventListener}s.
 * @see FoDataSourceImpl#addChangeEventListener(FodsEventListener)
 * @author szalik
 */
public class ChangeEventsThread extends Thread {
	private List<FodsEventListener> chaneEventListeners;
	private List<AbstractFodsEvent> waitingEvents;
	private Logger logger;



	public ChangeEventsThread(Logger logger) {
		super();
		setName("waitingEventsSender");
		setDaemon(true);

		this.logger = logger;
		this.chaneEventListeners = Collections.emptyList();
		this.waitingEvents = new ArrayList<AbstractFodsEvent>();
	}



	@Override
	public void run() {
		List<AbstractFodsEvent> eventsToSend = new LinkedList<AbstractFodsEvent>();
		while (true) {
			synchronized (waitingEvents) {
				if (waitingEvents.isEmpty()) try {
					waitingEvents.wait();
				} catch (InterruptedException e) {}
				eventsToSend.addAll(waitingEvents);
				waitingEvents.clear();
			}
			for (Iterator<AbstractFodsEvent> it = eventsToSend.iterator(); it.hasNext();) {
				AbstractFodsEvent e = it.next();
				it.remove();
				for (FodsEventListener l : chaneEventListeners) {
					try {
						l.onEvent(e);
					} catch (Exception ex) {
						logger.log(LogLevel.CRITICAL, "changeEventListener - " + l.getClass().getName() + " throws " + ex, ex);
					}
				}
			}
		} // while(true)
	}



	public void addChangeEventListener(FodsEventListener listener) {
		if (listener != null) {
			List<FodsEventListener> l = new LinkedList<FodsEventListener>();
			l.addAll(chaneEventListeners);
			l.add(listener);
			chaneEventListeners = l;
		}
	}



	public void notifyChangeEvent(AbstractFodsEvent event) {
		synchronized (waitingEvents) {
			waitingEvents.add(event);
			waitingEvents.notify();
		}
	}

}
