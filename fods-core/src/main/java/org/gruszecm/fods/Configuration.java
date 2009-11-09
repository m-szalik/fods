package org.gruszecm.fods;

import org.gruszecm.fods.impl.RecovererFactory;
import org.gruszecm.fods.log.Logger;


/**
 * 
 * @author szalik
 */
public class Configuration {
	private String testSql;
	private boolean autoRecovery;
	private boolean enableStats;
	private int backTime;
	private String recovererName;
	private boolean jmx;
	
	public Configuration() {
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
	
	
//	public String getDataSourceName(int index) {
//		StringBuilder builder = new StringBuilder();
//		builder.append('[').append(index).append(']');
//		if (dataSources.size() > index) {
//			builder.append(dataSources.get(index).getName());
//		} else {
//			builder.append("*NONE*");
//		}
//		return builder.toString();
//	}
		
	
	public void setTestSql(String testSql) {
		this.testSql = testSql;
	}
	
	public String getTestSql() {
		return testSql;
	}

	public void init(Logger logger) {
//		if (dataSources.isEmpty()) throw new IllegalStateException("call setDataSourcesJNDINames first.");
//		Collections.sort(dataSources, new Comparator<DataSourceWithName>() {
//			public int compare(DataSourceWithName o1, DataSourceWithName o2) {
//				return o1.getId() - o2.getId();		
//			}
//		});
//		try {
//			for(DataSourceWithName dswn : dataSources) {
//				dswn.init(logger);
//				logger.debug(dswn.getName() + " init ok :)");
//			}
//		} catch (Exception e) {
//			logger.log(LogLevel.CRITICAL, "Error building datasources", e);
//			throw new RuntimeException("Error building datasources", e);			
//		}
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





