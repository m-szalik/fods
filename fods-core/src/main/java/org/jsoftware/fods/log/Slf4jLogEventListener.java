package org.jsoftware.fods.log;

import org.jsoftware.fods.client.ext.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link LogEventListener} thats writes logs to slf4j {@link Logger}
 * @see DefaultLogger
 * @author szalik
 */
public class Slf4jLogEventListener implements LogEventListener {
	private Logger logger;



	public Slf4jLogEventListener() {
		logger = LoggerFactory.getLogger("org.jsoftware.fods");
	}



	public void logEvent(LogLevel level, String message, Throwable throwable) {
		if (level == LogLevel.DEBUG) {
			logger.debug(message, throwable);
		}
		if (level == LogLevel.INFO) {
			logger.info(message, throwable);
		}
		if (level == LogLevel.WARN) {
			logger.warn(message, throwable);
		}
	}
}
