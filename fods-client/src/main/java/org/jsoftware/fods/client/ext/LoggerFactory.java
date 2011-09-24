package org.jsoftware.fods.client.ext;

import java.util.Properties;


/**
 * Factory of {@link Logger}
 * @author szalik
 */
public interface LoggerFactory {

	Logger getLogger(Properties properties);

}
