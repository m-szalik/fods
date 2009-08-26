package org.gruszecm.fods.log;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author szalik
 */
public class PrintWriterLogEventListener implements LogEventListener {
	private boolean isDebugEnabled;
	private PrintWriter pw;	
	
	public PrintWriterLogEventListener(PrintWriter printWriter) throws IOException {
		pw = printWriter;
	}
	
	public void setDebug(boolean debug) {
		isDebugEnabled = debug;
	}

	private void write(LogLevel logLevel, String message, Throwable throwable) {
		try {
			pw.append("FODataSource ");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			pw.append(sdf.format(new Date())).append(' ');
			pw.append("::").append(message).append('\n');
			if (throwable != null) {
				throwable.printStackTrace(pw);
			}
			pw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void logEvent(LogLevel level, String message, Throwable throwable) {
		if (level.equals(LogLevel.DEBUG) && ! isDebugEnabled) return;
		else write(level, message, throwable);
	}

}
