package org.jsoftware.fods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCDriver;
import org.hsqldb.server.Server;
import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.impl.AbstractDataSourceFactory;
import org.jsoftware.fods.impl.PropertiesBasedConfigurationFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractDbTestTemplate {
	private DBHolder[] dbs = new DBHolder[] { new DBHolder(0), new DBHolder(1) };
	protected Configuration configuration;

	@BeforeClass
	public static void loadDriver() throws InstantiationException, IllegalAccessException, SQLException, IOException {
		JDBCDriver.class.newInstance();
		DriverManager.setLoginTimeout(10);
	}

	@Before
	public void startDbs() throws SQLException, IOException {
		for (DBHolder holder : dbs) {
			holder.start();
		}
	}

	@After
	public void stop() throws SQLException {
		for (DBHolder holder : dbs) {
			holder.stop();
		}
	}

	@Before
	public void prepareFODS() throws SQLException, IOException {
		PropertiesBasedConfigurationFactory propertiesConfigurationFactory = new PropertiesBasedConfigurationFactory();
		Properties properties = new Properties();
		properties.load(getClass().getResourceAsStream("testConfiguration.properties"));
		propertiesConfigurationFactory.setProperties(properties);
		configuration = propertiesConfigurationFactory.getConfiguration();
	}

	protected DataSource getFoDS() throws IOException {
		AbstractDataSourceFactory dsFactory = new AbstractDataSourceFactory() {
			@Override
			protected Configuration getConfiguration() throws IOException {
				return configuration;
			}
		};
		return dsFactory.getObjectInstance();
	}

	protected final void start(int i) throws SQLException, IOException {
		dbs[i].start();
	}

	protected final void stop(int i) throws SQLException {
		dbs[i].stop();
	}

	protected String getDbnameForConnection(Connection connection) throws SQLException {
		ResultSet rs = connection.createStatement().executeQuery("SELECT str_col FROM stable WHERE id=1");
		rs.next();
		String str = rs.getString(1);
		rs.close();
		return str;
	}

}

class DBHolder {
	Server server;
	int index;
	private boolean working;

	public DBHolder(int index) {
		this.index = index;
	}

	public void start() throws SQLException, IOException {
		if (!working) {
			Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:db" + index, "sa", "");
			BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/db" + index + ".sql")));
			String s;
			Statement stm = connection.createStatement();
			try {
				while ((s = br.readLine()) != null) {
					stm.execute(s);
					working = true;
				}
			} finally {
				br.close();
				stm.close();
				connection.close();
			}
		}
	}

	public void stop() throws SQLException {
		if (working) {
			Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:db" + index, "sa", "");
			connection.createStatement().execute("shutdown");
			working = false;
		}
	}
}
