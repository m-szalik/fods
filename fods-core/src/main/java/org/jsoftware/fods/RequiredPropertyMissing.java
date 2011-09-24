package org.jsoftware.fods;

public class RequiredPropertyMissing extends RuntimeException {
	
	public RequiredPropertyMissing(String dbname, String key) {
		super("Required configuration property \"" + key + "\" is missing for database " + dbname + ".");
	}

	public RequiredPropertyMissing(String key) {
		super("Required configuration property \"" + key + "\" is missing.");
	}

	private static final long serialVersionUID = -6435495378155327259L;

}
