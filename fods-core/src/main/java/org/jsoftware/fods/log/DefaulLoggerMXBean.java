package org.jsoftware.fods.log;

/**
 * MXBean for {@link DefaultLogger}
 * @author szalik
 */
public interface DefaulLoggerMXBean {

	boolean isDebug();



	void setDebug(boolean debug);



	boolean isLogEvents();



	void setLogEvents(boolean logEvents);



	boolean isLogOnConsole();



	void setLogOnConsole(boolean logOnConsole);

}
