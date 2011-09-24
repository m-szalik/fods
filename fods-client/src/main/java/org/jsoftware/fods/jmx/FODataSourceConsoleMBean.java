package org.jsoftware.fods.jmx;

import javax.sql.DataSource;

import org.jsoftware.fods.stats.Statistics;



/**
 * MxBean console for FODataSource.
 * @author szalik
 */
public interface FODataSourceConsoleMBean {
	
	/**
	 * @return get name of current {@link DataSource}
	 * @see #getCurrentDataSourceIndex()
	 */
	String getFodsName();
	
	/**
	 * @return get name of current database
	 */
	String getCurrentDatabaseName();	
	
	
	/**
	 * Change {@link DataSource} index
	 * @param index new database
	 */
	void setCurrentDatabaseName(String name);
	
	/**
	 * Test all {@link DataSource}
	 * @return test raport
	 */
	String test();
	
	/**
	 * Databases {@link Statistics} or <tt>null</tt> if statistics are disabled.
	 * @return
	 */
	Statistics getStatistics();
	
	
}
