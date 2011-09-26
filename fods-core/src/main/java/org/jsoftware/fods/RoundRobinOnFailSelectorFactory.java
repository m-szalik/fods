package org.jsoftware.fods;

import org.jsoftware.fods.client.ext.Configuration;
import org.jsoftware.fods.client.ext.FodsState;
import org.jsoftware.fods.client.ext.Selector;
import org.jsoftware.fods.client.ext.SelectorFactory;
import org.jsoftware.fods.impl.AbstractRoundRobinSelector;

/**
 * Factory for Round robin on database fail {@link Selector}. 
 * <p>Next database is selected only if a current one fail.</p>
 * @author szalik
 */
public class RoundRobinOnFailSelectorFactory implements SelectorFactory {
	
	public Selector getSelector(Configuration configuration) {
		return new AbstractRoundRobinSelector("RoundRobinOnFail", configuration) {
			public String select(FodsState fodsState) {
				String str = fodsState.getCurrentDatabase();
				if (str == null) {
					str = next(fodsState);
				}
				if (isValid(fodsState.getDbstate(str))) {
					return str;
				} else {
					return next(fodsState);
				}
			}
		};
	}
	
}
