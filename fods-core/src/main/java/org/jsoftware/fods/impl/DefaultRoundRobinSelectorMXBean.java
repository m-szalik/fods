package org.jsoftware.fods.impl;

import java.util.List;


/**
 * MxBean for {@link DefaultRoundRobinSelector}
 * @author szalik
 */
public interface DefaultRoundRobinSelectorMXBean {

	List<String> getSequence();
	
}
