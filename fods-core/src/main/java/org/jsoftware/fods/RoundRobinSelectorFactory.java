package org.jsoftware.fods;

import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.client.ext.FodsState;
import org.jsoftware.fods.client.ext.Selector;
import org.jsoftware.fods.client.ext.SelectorFactory;
import org.jsoftware.fods.impl.AbstractRoundRobinSelector;

/**
 * Factory for Round robin {@link Selector}.
 * <p>
 * Next database is selected every connection request.
 * </p>
 * @author szalik
 */
public class RoundRobinSelectorFactory implements SelectorFactory {

	public Selector getSelector(Configuration configuration) {
		return new AbstractRoundRobinSelector("RoundRobinOnFail", configuration) {
			public String select(FodsState fodsState) {
				return next(fodsState);
			}
		};
	}

}
