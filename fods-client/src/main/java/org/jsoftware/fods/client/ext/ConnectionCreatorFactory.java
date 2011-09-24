package org.jsoftware.fods.client.ext;

import java.util.Properties;

/**
 * Factory of {@link ConnectionCreator}.
 * @author szalik
 */
public interface ConnectionCreatorFactory {

	ConnectionCreator getConnectionCreator(String dbName, Logger logger,Properties properties);
	
}
