package org.jsoftware.fods.impl;

import java.util.List;


/**
 * MxBean for {@link AbstractRoundRobinSelector}
 * @author szalik
 */
public interface RoundRobinSelectorMXBean {

	List<String> getSequence();
	
}
