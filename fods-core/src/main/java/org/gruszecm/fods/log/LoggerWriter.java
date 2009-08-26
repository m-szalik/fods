package org.gruszecm.fods.log;

import java.io.IOException;
import java.io.Writer;

/**
 * 
 * @author szalik
 */
public class LoggerWriter extends Writer {
	private Logger logger;
	
	public LoggerWriter(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		logger.log(LogLevel.INFO, "LogWriter: " + new String(cbuf, off, len), null);
	}

}
