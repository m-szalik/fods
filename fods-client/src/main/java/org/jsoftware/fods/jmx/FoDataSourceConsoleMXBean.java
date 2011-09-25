package org.jsoftware.fods.jmx;

import javax.sql.DataSource;



/**
 * MxBean console for FODataSource.
 * <p>Allows to manage FoDataSource.</p>
 * @author szalik
 */
public interface FoDataSourceConsoleMXBean {
	
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
	 * @return <tt>true</tt> if operation successful.
	 */
	boolean forceSetCurrentDatabaseName(String name);
	
	/**
	 * Test all {@link DataSource}
	 * @return test report
	 */
	String testRaport();
	
	/**
	 * Test single database
	 * @param dbName
	 * @return test result, if <tt>null</tt> no database found
	 */
	Boolean test(String dbName);
	
	/**
	 * @return all database names
	 */
	String[] getDatabaseNames();
	
	/**
	 * Databases statistics or <tt>null</tt> if statistics are disabled.
	 * @return
	 */
	JMXStatistics[] getStatistics();
	
	/**
	 * Turned off databases sholdn't be used.
	 * @param dbName
	 * @return <tt>true</tt> if state changed.
	 */
	boolean turnOffDatabase(String dbName);
	
	/**
	 * Turn on database.
	 * @param dbName
	 * @return <tt>true</tt> if state changed.
	 */
	boolean turnOnDatabase(String dbName);
	
	/**
	 * @param dbName
	 * @return current database state, <tt>null</tt> if database not found.
	 */
	JMXFodsDbState getCurrentDatabaseState(String dbName);
}
