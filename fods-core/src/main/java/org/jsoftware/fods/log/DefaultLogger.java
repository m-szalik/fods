package org.jsoftware.fods.log;

import org.jsoftware.fods.client.ext.LogLevel;
import org.jsoftware.fods.client.ext.Logger;
import org.jsoftware.fods.client.ext.ManageableViaMXBean;
import org.jsoftware.fods.event.AbstractFodsEvent;
import org.jsoftware.fods.event.DatabaseFiledEvent;
import org.jsoftware.fods.event.RecoveryFailedEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Default {@link Logger} implementation.
 * @see DefaultLoggerFactory
 * @author szalik
 */
public class DefaultLogger implements Logger, ManageableViaMXBean {
	private Set<LogEventListener> listeners;
	private String fodsName = "";
	private boolean debug = true;
	private boolean logEvents;
	private boolean logOnConsole;



	public DefaultLogger(String fodsName) {
		this.listeners = Collections.emptySet();
		this.fodsName = fodsName;
		addLogEventListener(new ConsoleLogEventListener());
	}



	/*
	 * (non-Javadoc)
	 * @see org.jsoftware.fods.client.ext.Logger#info(java.lang.String)
	 */
	public void info(String msg) {
		log(LogLevel.INFO, msg, null);
	}



	/*
	 * (non-Javadoc)
	 * @see org.jsoftware.fods.client.ext.Logger#debug(java.lang.String)
	 */
	public void debug(String msg) {
		log(LogLevel.DEBUG, msg, null);
	}



	/*
	 * (non-Javadoc)
	 * @see org.jsoftware.fods.client.ext.Logger#warn(java.lang.String)
	 */
	public void warn(String msg) {
		log(LogLevel.WARN, msg, null);
	}



	/*
	 * (non-Javadoc)
	 * @see org.jsoftware.fods.client.ext.Logger#log(org.jsoftware.fods.client.ext.LogLevel, java.lang.String, java.lang.Throwable)
	 */
	public void log(LogLevel level, String message, Throwable throwable) {
		if (level == LogLevel.DEBUG && !debug) return;
		message = "[" + fodsName + "] " + message;
		for (LogEventListener l : listeners) {
			l.logEvent(level, message, throwable);
		}
	}



	/*
	 * (non-Javadoc)
	 * @see org.jsoftware.fods.client.ext.Logger#log(org.jsoftware.fods.client.ext.LogLevel, java.lang.String)
	 */
	public void log(LogLevel level, String msg) {
		log(level, msg, null);
	}



	/*
	 * (non-Javadoc)
	 * @see org.jsoftware.fods.client.ext.Logger#isDebugEnabled()
	 */
	public boolean isDebugEnabled() {
		return debug;
	}



	/*
	 * (non-Javadoc)
	 * @see org.jsoftware.fods.client.ext.Logger#logEvent(org.jsoftware.fods.event.AbstractFodsEvent)
	 */
	public void logEvent(AbstractFodsEvent event) {
		if (logEvents) {
			Throwable th = null;
			if (event instanceof DatabaseFiledEvent) {
				th = ((DatabaseFiledEvent) event).getReason();
			}
			if (event instanceof RecoveryFailedEvent) {
				th = ((RecoveryFailedEvent) event).getReason();
			}
			log(LogLevel.INFO, "* Event * " + event, th);
		}
	}



	void addLogEventListener(LogEventListener listener) {
		if (listener != null) {
			Set<LogEventListener> l = new HashSet<>(this.listeners);
			l.add(listener);
			this.listeners = l;
		}
	}



	/*
	 * (non-Javadoc)
	 * @see org.jsoftware.fods.client.ext.ManageableViaMXBean#getMXBeanInstance()
	 */
	public Object getMXBeanInstance() {
		return new DefaulLoggerMXBean() {
			public void setLogOnConsole(boolean logOnConsole) {
				DefaultLogger.this.logOnConsole = logOnConsole;
			}



			public void setLogEvents(boolean logEvents) {
				DefaultLogger.this.logEvents = logEvents;
			}



			public void setDebug(boolean debug) {
				DefaultLogger.this.debug = debug;
			}



			public boolean isLogOnConsole() {
				return DefaultLogger.this.logOnConsole;
			}



			public boolean isLogEvents() {
				return DefaultLogger.this.logEvents;
			}



			public boolean isDebug() {
				return DefaultLogger.this.debug;
			}
		};
	}



	public void setLogOnConsole(boolean console) {
		this.logOnConsole = console;
	}



	public void setLogEvents(boolean logEvents) {
		this.logEvents = logEvents;
	}



	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * @author szalik
	 */
	class ConsoleLogEventListener implements LogEventListener {
		public void logEvent(LogLevel level, String message, Throwable throwable) {
			if (logOnConsole) {
				System.out.println(level + ": " + message);
			}
		}
	}// ~
}
