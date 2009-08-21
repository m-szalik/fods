package pl.eo.apps.bossa.fods;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import pl.eo.apps.bossa.fods.dsfactory.DSFactory;
import pl.eo.apps.bossa.fods.impl.RecovererFactory;
import pl.eo.apps.bossa.fods.log.LogLevel;
import pl.eo.apps.bossa.fods.log.Logger;

/**
 * 
 * @author szalik
 */
public class Configuration {
	private List<DataSourceWithName> dataSources;
	private String testSql;
	private boolean autoRecovery;
	private boolean enableStats;
	private int backTime;
	private String recovererName;
	private boolean jmx;
	
	public Configuration() {
		this.dataSources = new LinkedList<DataSourceWithName>();
		this.testSql = "SELECT 2+1";
		this.autoRecovery = true;
		this.enableStats = false;
		this.backTime = 60;
		this.recovererName = "default";
	}
	
	public boolean isEnableStats() {
		return enableStats;
	}
	
	public boolean isJMX() {
		return jmx;
	}
	
	public void setJMX(boolean jmx) {
		this.jmx = jmx;
	}
	
	public void setEnableStats(boolean enableStats) {
		this.enableStats = enableStats;
	}
	
	public void setAutoRecovery(boolean autoRecovery) {
		this.autoRecovery = autoRecovery;
	}
	
	public boolean isAutoRecovery() {
		return autoRecovery;
	}
	
	public DataSource getDataSource(int index) {
		return dataSources.get(index).getDataSource();
	}
	
	public String getDataSourceName(int index) {
		StringBuilder builder = new StringBuilder();
		builder.append('[').append(index).append(']');
		if (dataSources.size() > index) {
			builder.append(dataSources.get(index).getName());
		} else {
			builder.append("*NONE*");
		}
		return builder.toString();
	}
	

	public int size() {
		return dataSources.size();
	}
	
	public void addDataSource(int id, String name, DSFactory dsFactory) {
		this.dataSources.add(new DataSourceWithName(name, id, dsFactory));
	}
	
	public void setTestSql(String testSql) {
		this.testSql = testSql;
	}
	
	public String getTestSql() {
		return testSql;
	}

	public void init(Logger logger) {
		if (dataSources.isEmpty()) throw new IllegalStateException("call setDataSourcesJNDINames first.");
		Collections.sort(dataSources, new Comparator<DataSourceWithName>() {
			public int compare(DataSourceWithName o1, DataSourceWithName o2) {
				return o1.getId() - o2.getId();		
			}
		});
		try {
			for(DataSourceWithName dswn : dataSources) {
				dswn.init(logger);
				logger.debug(dswn.getName() + " init ok :)");
			}
		} catch (Exception e) {
			logger.log(LogLevel.CRITICAL, "Error building datasources", e);
			throw new RuntimeException("Error building datasources", e);			
		}
		// try to get recoverer to validate configuration
		RecovererFactory.getRecoverer(this);
	}

	public void setRecovererName(String recovererName) {
		this.recovererName = recovererName;
	}
	
	public String getRecovererName() {
		return recovererName;
	}
	

	public void setBackTime(int backTime) {
		this.backTime = backTime;
	}
	
	public int getBackTime() {
		return backTime;
	}

}




class DataSourceWithName {
	private int id;
	private pl.eo.apps.bossa.fods.dsfactory.DSFactory dsFactory;
	private DataSource dataSource;
	private String name;
	
	public DataSourceWithName(String name, int id, DSFactory factory) {
		this.name = name;
		this.id = id;
		this.dsFactory = factory;
	}
	
	public int getId() {
		return id;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public String getName() {
		return name;
	}
		
	public void init(Logger logger) throws Exception {
		dataSource = dsFactory.getDataSource(logger);
	}
}
