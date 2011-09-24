package org.jsoftware.fods.impl;

import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.client.ext.Selector;
import org.jsoftware.fods.client.ext.SelectorFactory;

public class DefaultSelectorFactory implements SelectorFactory {
	
	public Selector getSelector(Configuration configuration) {
		return new DefaultRoundrobinSelector(configuration);
	}
}
