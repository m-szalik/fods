package org.jsoftware.fods.client.ext;

/**
 * Indicates that an {@link Object} can be displayed.
 * Can be applied to {@link Configuration}, {@link Configuration}, {@link Selector}, {@link ConnectionCreator} and many others componets.
 * @author szalik
 */
public interface Displayable {

	/**
	 * Display component informations
	 * @param addDebugInfo if <tt>true</tt> debug information can be added.
	 * @return displayable form of component
	 */
	String asString(boolean addDebugInfo);
	
}
