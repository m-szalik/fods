package pl.eo.apps.bossa.fods.impl;

import pl.eo.apps.bossa.fods.Configuration;
import pl.eo.apps.bossa.fods.Recoverer;

public class RecovererFactory {

	public static Recoverer getRecoverer(Configuration conf) {
		String name = conf.getRecovererName();
		if ("default".equalsIgnoreCase(name)) {
			return new RecovererImpl(conf);
		}
		throw new IllegalArgumentException("Can not find recoverer for name " + name);
	}
}
