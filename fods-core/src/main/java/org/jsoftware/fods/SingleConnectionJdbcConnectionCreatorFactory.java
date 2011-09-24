package org.jsoftware.fods;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.jsoftware.fods.client.ext.ConnectionCreator;
import org.jsoftware.fods.client.ext.ConnectionCreatorFactory;
import org.jsoftware.fods.client.ext.Logger;
import org.jsoftware.fods.impl.utils.PropertiesUtil;


/** Creates {@link ConnectionCreator} that uses pool of {@link Connection}s.
* <p>
* Required configuration values:
* <ul>
* 	<li>jdbcURI - database's jdbc uri</li>
*  <li>driverClassName - jdbc {@link Driver}</li>
*  <li><i>other jdbc connection properties</i></li>
* </ul>
* </p>
* @author szalik
*/
public class SingleConnectionJdbcConnectionCreatorFactory implements ConnectionCreatorFactory {

	public ConnectionCreator getConnectionCreator(final String dbname, final Logger logger, final Properties properties) {
		PropertiesUtil pu = new PropertiesUtil(properties, dbname);
		final String url = pu.getProperty("jdbcURI");
		pu.loadDriver("driverClassName");
		
		return new ConnectionCreator() {
			public Connection getConnection() throws SQLException {
				Connection con = DriverManager.getConnection(url, properties);
				logger.debug("New connection to \"" + dbname + "\" created.");
				return con;
			}
		};
	}
}

