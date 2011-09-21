package org.jsoftware.fods.client;

import javax.management.Notification;

import org.jsoftware.fods.event.AbstractChangeEvent;


public class EventNotification extends Notification {
	private static final long serialVersionUID = -5128253683032137315L;
	public static final String NTYPE = "jmx.fods.event";

	private AbstractChangeEvent event;
		
	public EventNotification(AbstractChangeEvent event, Object source, long sequenceNumber, long timestamp) {
		super(NTYPE, source, sequenceNumber, timestamp, event.toString());
		this.event = event;
	}
	
	public AbstractChangeEvent getEvent() {
		return event;
	}
	
}
