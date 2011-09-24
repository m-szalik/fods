package org.jsoftware.fods.impl;

import java.util.Properties;

import org.jsoftware.fods.client.ext.LogLevel;
import org.jsoftware.fods.client.ext.Logger;
import org.jsoftware.fods.client.ext.LoggerFactory;
import org.jsoftware.fods.log.CommonsLoggingLogEventListener;
import org.jsoftware.fods.log.DefaultLoggerImpl;
import org.jsoftware.fods.log.LogEventListener;
import org.jsoftware.fods.log.Slf4jLogEventListener;

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