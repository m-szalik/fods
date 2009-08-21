package pl.eo.apps.bossa.fods.dsfactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import pl.eo.apps.bossa.fods.log.LogLevel;
import pl.eo.apps.bossa.fods.log.Logger;

public class JndiDSFactory implements DSFactory {
	private String jndiName;
	
	public JndiDSFactory(String jndiName) {
		this.jndiName = jndiName;
	}

	public DataSource getDataSource(Logger logger) {
		try {
			InitialContext initContext = new InitialContext();
			Context envContext  = (Context) initContext.lookup("java:/comp/env");
			logger.info("Lookup for " + jndiName);
			DataSource dsin = (DataSource) envContext.lookup(jndiName);
			logger.info(jndiName + " found");
			return dsin;
		} catch (NamingException e) {
			logger.log(LogLevel.CRITICAL, "Error building datasources", e);
			throw new RuntimeException("Error building datasources", e);			
		}
	}

	
}
