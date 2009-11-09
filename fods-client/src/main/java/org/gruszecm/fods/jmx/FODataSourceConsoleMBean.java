package org.gruszecm.fods.jmx;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.gruszecm.fods.stats.Statistics;



/**
 * MxBean console for FODataSource
 * @author szalik
 */
public interface FODataSourceConsoleMBean {
	
	/**
	 * @return get name of current {@link DataSource}
	 * @see #getCurrentDataSourceIndex()
	 */
	String getCurrentDataSourceName();
	
	/**
	 * @return get index of current {@link DataSource}
	 */
	int getCurrentDataSourceIndex();	
	
	Boolean getCurrentConnectionReadOnly();
	
	/**
	 * @return
	 */
	boolean isAutoRecovery();
	
	/**
	 * @param b
	 */
	void setAutoRecovery(boolean b);
	
	/**
	 * @return number of seconds to next recovery procedure, <tt>null</tt> if there is no recovery procedure in progress
	 */
	Integer getNextRecoveryCountdown();
	
	/**
	 * @return recovery procedure delay [sec]
	 */
	int getBackTime();
	
	/**
	 * @param sec recovery procedure delay [sec]
	 */
	void setBackTime(int sec);
	
	/**
	 * Change {@link DataSource} index
	 * @param index new {@link DataSource} index
	 * @see #getCurrentDataSourceIndex()
	 */
	void setCurrentDataSourceIndex(int index);
	
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
	
	
	/**
	 * Check if {@link Connection} on <code>index</code> is readOnly 
	 * @see Connection#isReadOnly()
	 * @param index
	 * @return <tt>null<tt> if error ocured while getting {@link Connection}
	 * @throws SQLException 
	 */
	Boolean isConnectionReadOnly(int index) throws SQLException;
}
