package org.jsoftware.fods.log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import org.jsoftware.fods.client.ext.LogLevel;
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
 *  <li>File if property <i>loggerFile</i> is set.</li>
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
		
		String logFile = pu.getProperty("loggerFile", "").trim();
		if (logFile.length() > 0) {
			logger.addLogEventListener(new FileLogEventListener(new File(logFile)));
			console = false;
		}
		
		if (Boolean.valueOf(properties.getProperty("loggerForceLogOnConsole"))) {
			console = true;
		}
		logger.setLogOnConsole(console);
		logger.setLogEvents(Boolean.valueOf(properties.getProperty("loggerLogEvents")));
		
		return logger;
	}	
}

class FileLogEventListener implements LogEventListener {
	private PrintWriter writer;
	
	public FileLogEventListener(File file) {
		boolean ok = true;
		if (! file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				ok = false;
			}
		}
		if (! file.canWrite()) ok = false;
		if (! ok) {
			System.err.println("Can not create / write to file " + file.getAbsolutePath());
		} else {
			try {
				writer = new PrintWriter(file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	} 

	@Override
	public void logEvent(LogLevel level, String message, Throwable throwable) {
		if (writer != null) {
			writer.append(level.name()).append('\t').append(message);
			if (throwable != null) {
				throwable.printStackTrace(writer);
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		try {
			if (writer != null) {
				writer.close();
			}
		} catch (Exception e) {		}
		super.finalize();
	}
	
}
