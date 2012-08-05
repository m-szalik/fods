package org.jsoftware.fods.impl;

import java.util.Map;

import javax.management.MXBean;

/**
 * MXBean for {@link DefaultConfiguration}
 * @author szalik
 */
@MXBean
public interface DefaultConfigurationMXBean {

	Map<String, String> getGlobalConfigurationValues();

}
