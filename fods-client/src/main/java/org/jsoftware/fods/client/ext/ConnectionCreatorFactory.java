package org.jsoftware.fods.client.ext;

import java.util.Properties;

public interface ConnectionCreatorFactory {

	ConnectionCreator getConnectionCreator(String dbName, Logger logger,Properties properties);
	
}
