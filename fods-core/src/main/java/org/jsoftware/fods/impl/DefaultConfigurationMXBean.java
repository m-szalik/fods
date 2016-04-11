package org.jsoftware.fods.impl;

import javax.management.MXBean;
import java.util.Map;

/**
 * MXBean for {@link DefaultConfiguration}
 * @author szalik
 */
@MXBean
public interface DefaultConfigurationMXBean {

	Map<String, String> getGlobalConfigurationValues();

}
