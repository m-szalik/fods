package org.jsoftware.fods.log;

import java.util.Properties;

import org.jsoftware.fods.client.ext.LogLevel;
import org.jsoftware.fods.client.ext.Logger;
import org.jsoftware.fods.client.ext.LoggerFactory;

/**
 * Factory for {@link DefaultLoggerImpl}.
 * <p>Default {@link Logger} writes logs to:
 * <ul>
 * 	<li>Slf4j if found.</li>
 * 	<li>Commons logging if found.</li>
 * 	<li>Console if none of above was found.</li>
 * </ul>
 * </p>
 * @author szalik
 */
public class DefaultLoggerFactory implements LoggerFactory {

	public Logger getLogger(Properties properties) {
		String fodsName = properties.getProperty("fodsName");
		Boolean b = Boolean.valueOf(properties.getProperty("loggerDebugOn", "false"));
		DefaultLoggerImpl logger = new DefaultLoggerImpl(fodsName);
		logger.setDebug(b);
		boolean console = true;
		try {
			Class.forName("org.slf4j.LoggerFactory");
			console = false;
			logger.addLogEventListener(new Slf4jLogEventListener());
		} catch (ClassNotFoundException e) {	}
		try {
			Class.forName("org.apache.commons.logging.LogFactory");
			console = false;
			logger.addLogEventListener(new CommonsLoggingLogEventListener());
		} catch (ClassNotFoundException e) {	}
		if (console) {
			logger.addLogEventListener(new ConsoleLogEventListener());
		}
		return logger;
	}
	
}


class ConsoleLogEventListener implements LogEventListener {
	public void logEvent(LogLevel level, String message, Throwable throwable) {
		System.out.println(level + ": " + message);
	}
}