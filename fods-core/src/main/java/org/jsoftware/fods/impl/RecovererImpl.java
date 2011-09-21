package org.jsoftware.fods.impl;

import org.jsoftware.fods.Configuration;
import org.jsoftware.fods.Recoverer;


/**
 * 
 * @author szalik
 */
public class RecovererImpl implements Recoverer {
	private Configuration configuration;
	private long recoveryTS;
	private int tryNo = 0;
		
	public RecovererImpl(Configuration configuration) {
		this.configuration = configuration;
		calculateRecoveryTimestamp();		
	}

	private void calculateRecoveryTimestamp() {
		int tw = tryNo > 3 ? 3 : tryNo;
		tw = 1; // nie chcemy uzywac "tryNo", ale jakby ktos chcial to mozna.
		this.recoveryTS = System.currentTimeMillis() + tw * 1000 * configuration.getBackTime();
	}
	
	/* (non-Javadoc)
	 * @see org.jsoftware.fods.Recoverer#canRecovery()
	 */
	public boolean canRecovery() {
		if (! configuration.isAutoRecovery()) return false;
		if (System.currentTimeMillis() > recoveryTS) return true;
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.jsoftware.fods.Recoverer#getRecoveryTimestamp()
	 */
	public long getRecoveryTimestamp() {
		return recoveryTS;
	}

	/* (non-Javadoc)
	 * @see org.jsoftware.fods.Recoverer#recoveryFailed()
	 */
	public void recoveryFailed() {
		if (tryNo < Integer.MAX_VALUE) {
			tryNo++;
		}
		calculateRecoveryTimestamp();
	}
}
