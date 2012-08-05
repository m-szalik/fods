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
import org.jsoftware.fods.impl.AbstractFoDataSourceFactory;
import org.jsoftware.fods.impl.FoDataSourceImpl;
import org.jsoftware.fods.impl.PropertiesBasedConfigurationFactory;

/**
 * Factory of {@link FoDataSourceImpl} object.
 * <p>
 * Use this factory to place {@link FoDataSourceImpl} into {@link InitialContext}.
 * </p>
 * @author szalik
 * @see also {@link FoDataSource}.
 */
public class J2EEFoDataSourceObjectFactory implements ObjectFactory {

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
				ins = getClass().getResourceAsStream(pl.getContent().toString());
				if (ins == null) {
					try {
						ins = new FileInputStream(pl.getContent().toString());
					} catch (IOException e) {
						ins = null;
					}
				}
				if (ins == null) {
					throw new IOException("Can not load foDS properties form " + pl.getContent());
				}
				properties.load(ins);
				ins.close();
			}
			for (Enumeration<RefAddr> en = r.getAll(); en.hasMoreElements();) {
				RefAddr ra = en.nextElement();
				String key = ra.getType();
				key = key.replace('_', '.');
				properties.setProperty(key, ra.getContent().toString());
			} // for

			AbstractFoDataSourceFactory factory = new AbstractFoDataSourceFactory() {
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
