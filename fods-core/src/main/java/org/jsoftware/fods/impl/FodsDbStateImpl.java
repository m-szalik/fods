package org.jsoftware.fods.impl;

import org.jsoftware.fods.client.ext.FodsDbState;
import org.jsoftware.fods.client.ext.FodsDbStateStatus;

import java.io.Serializable;
import java.sql.SQLException;

/**
 * Single database state.
 * @author szalik
 */
public class FodsDbStateImpl implements Serializable, FodsDbState {
	private static final long serialVersionUID = 5170522522608328083L;
	private long brokenTS;
	private SQLException lastException;
	private FodsDbStateStatus status = FodsDbStateStatus.VALID;
	private boolean readonly;



	public long getBrokenTime() {
		if (status == FodsDbStateStatus.BROKEN) {
			return System.currentTimeMillis() - brokenTS;
		}
		return -1;
	}



	public FodsDbStateStatus getStatus() {
		return status;
	}



	public boolean isReadOnly() {
		return readonly;
	}



	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}



	public void setState(FodsDbStateStatus status) {
		if (status != this.status && status == FodsDbStateStatus.BROKEN) {
			brokenTS = System.currentTimeMillis();
		}
		this.status = status;
	}



	public SQLException getLastException() {
		return status == FodsDbStateStatus.BROKEN ? lastException : null;
	}



	public void setLastException(SQLException lastException) {
		this.lastException = lastException;
	}

}
