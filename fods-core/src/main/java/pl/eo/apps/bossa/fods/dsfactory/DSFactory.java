package pl.eo.apps.bossa.fods.dsfactory;

import javax.sql.DataSource;

import pl.eo.apps.bossa.fods.log.Logger;

public interface DSFactory {
	
	DataSource getDataSource(Logger logger);
	
}
