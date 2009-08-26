package org.gruszecm.fods.dsfactory;

import javax.sql.DataSource;

import org.gruszecm.fods.log.Logger;


public interface DSFactory {
	
	DataSource getDataSource(Logger logger);
	
}
