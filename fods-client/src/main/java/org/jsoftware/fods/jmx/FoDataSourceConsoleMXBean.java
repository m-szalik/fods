package org.jsoftware.fods.jmx;

import javax.management.MXBean;
import javax.sql.DataSource;

/**
 * MxBean console for FODataSource.
 * <p>
 * Allows to manage FoDataSource.
 * </p>
 * @author szalik
 */
@MXBean
public interface FoDataSourceConsoleMXBean {

	/**
	 * @return get name of current {@link DataSource}
	 * @see #getCurrentDatabaseName()
	 */
	String getFodsName();



	/**
	 * @return get name of current database, <tt>null</tt> means not set.
	 */
	String getCurrentDatabaseName();



	/**
	 * Change {@link DataSource} index
	 * @param name new database
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
	 * @param dbName database name to be tested
	 * @return test result
	 */
	boolean test(String dbName);



	/**
	 * @return all database names
	 */
	String[] getDatabaseNames();



	/**
	 * Databases statistics or <tt>null</tt> if statistics are disabled.
	 * @return statistics for all databases
	 */
	JMXStatistics[] getStatistics();



	/**
	 * Turned off databases sholdn't be used.
	 * @param dbName database name
	 * @return <tt>true</tt> if state changed.
	 */
	boolean turnOffDatabase(String dbName);



	/**
	 * Turn on database.
	 * @param dbName database name
	 * @return <tt>true</tt> if state changed.
	 */
	boolean turnOnDatabase(String dbName);



	/**
	 * @param dbName database name
	 * @return current database state, <tt>null</tt> if database not found.
	 */
	JMXFodsDbState getCurrentDatabaseState(String dbName);
}
