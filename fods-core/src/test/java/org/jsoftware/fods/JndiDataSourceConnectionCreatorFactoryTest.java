package org.jsoftware.fods;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Assert;

import org.jsoftware.fods.client.ext.ConnectionCreator;
import org.jsoftware.fods.log.DefaultLogger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class JndiDataSourceConnectionCreatorFactoryTest {
	
	private static final String JNDI_DS = "remoteDS";

	@Before
	public void putDataSourceIntoInitialContext() throws NamingException {
		InitialContext ctx = new InitialContext();
		ctx.bind("java:/comp/env/" + JNDI_DS, new Object());
	}

	@SuppressWarnings("deprecation")
	@Test @Ignore
	public void test() {
		JndiDataSourceConnectionCreatorFactory factory = new JndiDataSourceConnectionCreatorFactory();
		Properties properties = new Properties();
		properties.setProperty("jndiName", JNDI_DS);
		ConnectionCreator cc = factory.getConnectionCreator("testDB", new DefaultLogger("testFODS"), properties);
		Assert.assertNotNull(cc);
	}
}
