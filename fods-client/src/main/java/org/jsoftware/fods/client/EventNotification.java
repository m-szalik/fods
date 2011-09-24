package org.jsoftware.fods.client;

import javax.management.Notification;

import org.jsoftware.fods.event.AbstractFodsEvent;


public class EventNotification extends Notification {
	private static final long serialVersionUID = -5128253683032137315L;
	public static final String NTYPE = "jmx.fods.event";

	private AbstractFodsEvent event;
		
	public EventNotification(AbstractFodsEvent event, Object source, long sequenceNumber, long timestamp) {
		super(NTYPE, source, sequenceNumber, timestamp, event.toString());
		this.event = event;
	}
	
	public AbstractFodsEvent getEvent() {
		return event;
	}
	
}
