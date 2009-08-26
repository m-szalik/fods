package org.gruszecm.fods.impl;

import org.gruszecm.fods.Configuration;
import org.gruszecm.fods.Recoverer;

public class RecovererFactory {

	public static Recoverer getRecoverer(Configuration conf) {
		String name = conf.getRecovererName();
		if ("default".equalsIgnoreCase(name)) {
			return new RecovererImpl(conf);
		}
		throw new IllegalArgumentException("Can not find recoverer for name " + name);
	}
}
