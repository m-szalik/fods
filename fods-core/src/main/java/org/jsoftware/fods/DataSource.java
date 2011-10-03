package org.jsoftware.fods;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * An alias for {@link FoDataSource} 
 * @author szalik
 */
public class DataSource extends FoDataSource {

	public DataSource(InputStream inputStream) throws IOException {
		super(inputStream);
	}

	public DataSource(String location) throws IOException {
		super(location);
	}
	
	public DataSource(Properties props) throws IOException {
		super(props);
	}

}
