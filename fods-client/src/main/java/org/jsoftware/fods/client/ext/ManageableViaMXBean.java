package org.jsoftware.fods.client.ext;

import javax.management.MXBean;

/**
 * If you want to register a {@link MXBean} for your implementation of {@link Logger}, {@link ConnectionCreator} or {@link Selector} that implementation must implement also
 * {@link ManageableViaMXBean} interface.
 * @see Logger
 * @see Configuration
 * @see Selector
 * @author szalik
 */
public interface ManageableViaMXBean {

	/**
	 * @return {@link MXBean} to register
	 */
	Object getMXBeanInstance();

}
