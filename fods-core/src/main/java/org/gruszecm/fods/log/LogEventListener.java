package org.gruszecm.fods.log;

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
