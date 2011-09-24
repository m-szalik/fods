package org.jsoftware.fods.client.ext;


/**
 * Single database state.
 * @author szalik
 */
public class FodsDbState {
	
	private long brokenTS;
	private FodsDbStateStatus status = FodsDbStateStatus.VALID;
	
	/**
	 * @return number of ms when last database error occurred.
	 */
	public long getBrokenTime() {
		if (status == FodsDbStateStatus.BROKEN) {
			return System.currentTimeMillis() - brokenTS;
		}
		return -1;
	}
	
	/**
	 * <font color="red">Do not use this method.</font>
	 * @param status
	 */
	public void setState(FodsDbStateStatus status) {
		if (status != this.status && status == FodsDbStateStatus.BROKEN) {
			brokenTS = System.currentTimeMillis();
		}
		this.status = status;
	}
	
	/**
	 * @return current database {@link STATE}
	 */
	public FodsDbStateStatus getStatus(){
		return status;
	}
	
	
}
