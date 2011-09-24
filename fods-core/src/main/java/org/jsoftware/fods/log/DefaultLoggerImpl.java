package org.jsoftware.fods.log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jsoftware.fods.client.ext.LogLevel;
import org.jsoftware.fods.client.ext.Logger;

/**
 * Default {@link Logger} implementation.
 * @see DefaultLoggerFactory
 * @author szalik
 */
public class DefaultLoggerImpl implements Logger {
	private Set<LogEventListener> listeners;
	private String fodsName = "";
	private boolean debug = true;
	
	public DefaultLoggerImpl(String fodsName) {
		listeners = Collections.emptySet();
		this.fodsName = fodsName;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public void info(String msg) {
		log(LogLevel.INFO, msg, null);
	}
	
	public void debug(String msg) {
		log(LogLevel.DEBUG, msg, null);
	}
	
	
	public void warn(String msg) {
		log(LogLevel.WARN, msg, null);
	}
		
	public void log(LogLevel level, String message, Throwable throwable) {
		if (level == LogLevel.DEBUG && ! debug) return;
		message = "[" + fodsName + "] " + message;
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
