package org.jsoftware.fods;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;

import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.impl.AbstractDataSourceFactory;
import org.jsoftware.fods.impl.FODataSource;
import org.jsoftware.fods.impl.PropertiesBasedConfigurationFactory;

/**
 * Factory of {@link FODataSource} object.
 * <p>Use this factory to place {@link FODataSource} into {@link InitialContext}.</p> 
 * @author szalik
 */
public class J2EEFODataSourceFactory implements ObjectFactory {

	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
		if (obj instanceof Reference) {
			final Properties properties = new Properties();
			Reference r = (Reference) obj;
			RefAddr pl = r.get("location");
			if (pl != null) {
				InputStream ins;
				if (pl.getContent() instanceof File) {
					ins = new FileInputStream((File) pl.getContent());
				}
				if (pl.getContent() instanceof InputStream) {
					ins = (InputStream) pl.getContent();
				}
				ins = new FileInputStream(pl.getContent().toString());
				properties.load(ins);
			}
			for (Enumeration<RefAddr> en = r.getAll(); en.hasMoreElements();) {
				RefAddr ra = en.nextElement();
				properties.setProperty(ra.getType(), ra.getContent().toString());
			} // for

			AbstractDataSourceFactory factory = new AbstractDataSourceFactory() {
				@Override
				protected Configuration getConfiguration() throws IOException {
					PropertiesBasedConfigurationFactory factory = new PropertiesBasedConfigurationFactory();
					factory.setProperties(properties);
					return factory.getConfiguration();
				}
				
			};
			DataSource ds = factory.getObjectInstance();
			return ds;
		} else {
			throw new RuntimeException(obj + " is not " + Reference.class.getName());
		}
	}

}
