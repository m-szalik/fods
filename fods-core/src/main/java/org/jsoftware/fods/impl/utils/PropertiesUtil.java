package org.jsoftware.fods.impl.utils;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Helps to read {@link Properties}.
 * 
 * @author szalik
 */
public class PropertiesUtil {

	private String dbName;
	private Properties properties;

	public PropertiesUtil(Properties properties) {
		this.properties = properties;
	}

	public PropertiesUtil(Properties properties, String dbName) {
		this(properties);
		this.dbName = dbName;
	}

	public String getProperty(String key) {
		String p = getProperty(key, null);
		if (p == null) {
			throw dbName == null ? new RequiredPropertyMissing(key) : new RequiredPropertyMissing(dbName, key);
		}
		return p;
	}

	public String getProperty(String key, String defaultValue) {
		String prop = properties.getProperty(key);
		if (prop == null) {
			prop = defaultValue;
		}
		return prop;
	}

	public void loadDriver(String key) {
		String className = getProperty(key);
		try {
			Driver driver = (Driver) Class.forName(className).newInstance();
			DriverManager.registerDriver(driver);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Driver class not found for database \"" + dbName + "\", dirverClassName=" + className, e);
		} catch (Exception e) {
			throw new RuntimeException("Can not load jdbc driver for database \"" + dbName + "\", dirverClassName= " + className, e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T load(String key, Class<T> defaultImpl) {
		String className = defaultImpl != null ? getProperty(key, "") : getProperty(key);
		Class<T> cl = null;
		if (className.length() > 0) {
			try {
				cl = (Class<T>) Class.forName(className);
			} catch (ClassNotFoundException e) {
				if (dbName != null) {
					throw new RuntimeException("Class not found for database \"" + dbName + "\", className=" + className, e);
				} else {
					throw new RuntimeException("Class not found className=" + className, e);
				}
			}
		} else {
			cl = defaultImpl;
			className = defaultImpl.getName();
		}
		try {
			return cl.newInstance();
		} catch (Exception e) {
			if (dbName != null) {
				throw new RuntimeException("Object can not be created for database \"" + dbName + "\", className=" + className, e);
			} else {
				throw new RuntimeException("Object can not be created, className=" + className, e);
			}
		}
	}

	public Set<String> getPropertyKeys() {
		HashSet<String> keys = new HashSet<String>();
		for(Object o : properties.keySet()) {
			if (o != null) {
				keys.add(o.toString());
			}
		}
		return keys;
	}

}
