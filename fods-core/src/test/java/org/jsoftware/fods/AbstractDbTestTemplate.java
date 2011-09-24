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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractDbTestTemplate {
	private static DBHolder[] dbs = new DBHolder[] { new DBHolder(0), new DBHolder(1) };
	protected Configuration configuration;

	@BeforeClass
	public static void loadDriver() throws InstantiationException, IllegalAccessException, SQLException, IOException {
		JDBCDriver.class.newInstance();
		DriverManager.setLoginTimeout(10);
		for (DBHolder holder : dbs) {
			holder.create();
		}
	}
	
	@AfterClass
	public static void shutdown() {
		for (DBHolder holder : dbs) {
			holder.server.shutdown();
			holder.stop();
		}
	}
	
	@Before
	public void start() {
		for(DBHolder holder : dbs) {
			holder.start();
		}
	}
	
	@After
	public void stop() {
		for(DBHolder holder : dbs) {
			holder.stop();
		}
	}

	@Before
	public void prepareFODS() throws SQLException, IOException {
		PropertiesBasedConfigurationFactory propertiesConfigurationFactory = new PropertiesBasedConfigurationFactory();
		Properties properties = new Properties();
		properties.load(getClass().getResourceAsStream("testConfiguration.properties"));
		for (DBHolder dbh : dbs) {
			String pkey = "db" + dbh.index + ".jdbcURI";
			if (properties.containsKey(pkey)) {
				properties.setProperty(pkey, dbh.getJDBCUri());
			}
		}
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
	
	protected final void start(int i) {
		dbs[i].start();
	}

	protected final void stop(int i) {
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
		int port = 5555;
		private boolean working;

		public DBHolder(int index) {
			this.index = index;
			this.server = new Server();
			this.server.setDaemon(false);
			this.server.setDatabaseName(0, "db" + index);
			this.server.setDatabasePath(0, "mem:db" + index);
			this.server.setAddress("localhost");
		}

		public boolean isWorking() {
			return working;
		}
		
		public String getJDBCUri() {
			return "jdbc:hsqldb:hsql://localhost:" + port + "/db" + index;
		}

		public void create() throws SQLException, IOException {
			port = port + index;
			server.setPort(port);
			start();
			Connection connection = DriverManager.getConnection(getJDBCUri(), "sa", "");
			BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/db" + index + ".sql")));
			String s;
			Statement stm = connection.createStatement();
			try {
				while ((s = br.readLine()) != null) {
					stm.execute(s);
				}
			} finally {
				br.close();
				stm.close();
				connection.close();
			}
		}

		public void start() {
			server.start();
			working = true;
		}

		public void stop() {
			server.stop();
			working = false;
		}
	}
