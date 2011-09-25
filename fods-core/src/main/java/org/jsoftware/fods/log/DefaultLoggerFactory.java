package org.jsoftware.fods.log;

import java.util.Properties;

import org.jsoftware.fods.client.ext.Logger;
import org.jsoftware.fods.client.ext.LoggerFactory;
import org.jsoftware.fods.impl.utils.PropertiesUtil;

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
		PropertiesUtil pu = new PropertiesUtil(properties);
		DefaultLogger logger = new DefaultLogger(pu.getProperty("fodsName"));
		logger.setDebug(Boolean.valueOf(pu.getProperty("loggerDebugOn")));

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
		
		if (Boolean.valueOf(properties.getProperty("loggerForceLogOnConsole"))) {
			console = true;
		}
		logger.setLogOnConsole(console);
		logger.setLogEvents(Boolean.valueOf(properties.getProperty("loggerLogEvents")));
		
		return logger;
	}
	
}
