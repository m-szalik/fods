package org.jsoftware.fods.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoftware.fods.client.ext.LogLevel;

public class CommonsLoggingLogEventListener implements LogEventListener {
	private Log logger;
	
	public CommonsLoggingLogEventListener() {
		logger = LogFactory.getLog("org.jsoftware.fods");
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
