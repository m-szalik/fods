package org.jsoftware.fods;

/**
 * 
 * @author szalik
 */
public interface Recoverer {

	/**
	 * Check if recovery procedure can be started
	 * @return
	 */
	boolean canRecovery();

	long getRecoveryTimestamp();

	void recoveryFailed();

}