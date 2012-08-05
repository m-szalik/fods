package org.jsoftware.fods;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.impl.AbstractFoDataSourceFactory;
import org.jsoftware.fods.impl.PropertiesBasedConfigurationFactory;

/**
 * Fail over {@link DataSource}
 * @author szalik
 */
public class FoDataSource implements DataSource {
	private DataSource fods;



	public FoDataSource(Properties props) throws IOException {
		setup(props);
	}



	public FoDataSource(InputStream inputStream) throws IOException {
		if (inputStream == null) {
			throw new IllegalArgumentException("InputStream can not be null.");
		}
		Properties props = new Properties();
		props.load(inputStream);
		setup(props);
	}



	public FoDataSource(String loaction) throws IOException {
		Properties props = new Properties();
		InputStream ins = FoDataSource.class.getResourceAsStream(loaction);
		try {
			if (ins == null) {
				File file = new File(loaction);
				if (file.exists()) {
					ins = new FileInputStream(file);
				}
			}
			if (ins == null) {
				throw new IOException("Can not load configuration from location " + loaction);
			}
			props.load(ins);
		} finally {
			if (ins != null) {
				ins.close();
			}
		}
		setup(props);
	}



	private void setup(final Properties props) throws IOException {
		AbstractFoDataSourceFactory factory = new AbstractFoDataSourceFactory() {
			@Override
			protected Configuration getConfiguration() throws IOException {
				PropertiesBasedConfigurationFactory factory = new PropertiesBasedConfigurationFactory();
				factory.setProperties(props);
				return factory.getConfiguration();
			}
		};
		fods = factory.getObjectInstance();
	}



	public PrintWriter getLogWriter() throws SQLException {
		return fods.getLogWriter();
	}



	public void setLogWriter(PrintWriter out) throws SQLException {
		fods.setLogWriter(out);
	}



	public void setLoginTimeout(int seconds) throws SQLException {
		fods.setLoginTimeout(seconds);
	}



	public int getLoginTimeout() throws SQLException {
		return fods.getLoginTimeout();
	}



	public <T> T unwrap(Class<T> iface) throws SQLException {
		return fods.unwrap(iface);
	}



	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return fods.isWrapperFor(iface);
	}



	public Connection getConnection() throws SQLException {
		return fods.getConnection();
	}



	public Connection getConnection(String username, String password) throws SQLException {
		return fods.getConnection(username, password);
	}
}
