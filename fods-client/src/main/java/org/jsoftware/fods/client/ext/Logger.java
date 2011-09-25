package org.jsoftware.fods.client.ext;

import org.jsoftware.fods.event.AbstractFodsEvent;

/**
 * @author szalik
 * @see ManageableViaMXBean
 */
public interface Logger {
	
	void info(String msg);
	
	void debug(String msg);
	
	void warn(String msg);
			
	void log(LogLevel level, String message, Throwable throwable);
	
	void log(LogLevel level, String msg);
	
	void logEvent(AbstractFodsEvent event);

	boolean isDebugEnabled();
	
}