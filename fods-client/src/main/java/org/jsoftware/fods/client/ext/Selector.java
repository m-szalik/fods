package org.jsoftware.fods.client.ext;


/**
 * 
 * Database selection strategy.
 * @author szalik
 */
public interface Selector {

	void init(DatabasesState state);

	int select();	

	void onStateChanged();

	void forceSetIndex(int index) throws UnsupportedOperationException;
}
