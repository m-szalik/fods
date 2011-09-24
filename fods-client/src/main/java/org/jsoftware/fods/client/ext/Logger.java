package org.jsoftware.fods.client.ext;

/**
 * 
 * @author szalik
 */
public interface Logger {
	
	public void info(String msg);
	
	public void debug(String msg);
	
	public void warn(String msg);
			
	public void log(LogLevel level, String message, Throwable throwable);
	
	public void log(LogLevel level, String msg);
}