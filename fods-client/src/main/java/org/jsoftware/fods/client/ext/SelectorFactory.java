package org.jsoftware.fods.client.ext;

/**
 * Factory of {@link Selector}
 * @author szalik
 */
public interface SelectorFactory {
	
	Selector getSelector(Configuration configuration);
	
}
