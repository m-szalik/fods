package org.jsoftware.fods.log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author szalik
 */
public class Logger {
	private Set<LogEventListener> listeners;
	
	public Logger() {
		listeners = Collections.emptySet();
	}
	
	public void info(String msg) {
		log(LogLevel.INFO, msg, null);
	}
	
	public void debug(String msg) {
		log(LogLevel.DEBUG, msg, null);
	}
	
		
	public void log(LogLevel level, String message, Throwable throwable) {
		for(LogEventListener l : listeners) {
			l.logEvent(level, message, throwable);
		}
	}
	
	public void log(LogLevel level, String msg) {
		log(level, msg, null);
	}
	
	public void addLogEventListener(LogEventListener listener) {
		if (listener != null) {
			Set<LogEventListener> l = new HashSet<LogEventListener>(this.listeners);
			l.add(listener);
			this.listeners = l;
		}
	}

	
			
}
