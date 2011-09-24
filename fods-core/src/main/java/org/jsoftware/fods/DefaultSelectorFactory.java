package org.jsoftware.fods;

import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.client.ext.Selector;
import org.jsoftware.fods.client.ext.SelectorFactory;
import org.jsoftware.fods.impl.DefaultRoundRobinSelector;

/**
 * Factory for {@link DefaultRoundRobinSelector}.
 * @see DefaultRoundRobinSelector
 * @author szalik
 */
public class DefaultSelectorFactory implements SelectorFactory {
	
	public Selector getSelector(Configuration configuration) {
		return new DefaultRoundRobinSelector(configuration);
	}
}
