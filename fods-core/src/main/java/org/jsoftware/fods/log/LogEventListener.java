package org.jsoftware.fods.log;

import org.jsoftware.fods.client.ext.LogLevel;

/**
 * 
 * @author szalik
 */
public interface LogEventListener {

	/**
	 * @param level
	 * @param message
	 * @param throwable can be <tt>null</tt>
	 */
	void logEvent(LogLevel level, String message, Throwable throwable);
	
}
