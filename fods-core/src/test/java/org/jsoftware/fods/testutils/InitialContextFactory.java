package org.jsoftware.fods.testutils;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;

public class InitialContextFactory implements javax.naming.spi.InitialContextFactory {
	private static Context ctx;
	
	public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
		if (ctx == null) {
			ctx = null; // TODO
		}
		return ctx;
	}

}
