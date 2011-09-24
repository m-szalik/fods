package org.jsoftware.fods.client.ext;


public class FodsDbState {

	public enum STATE {
		VALID, DISCONNETED, BROKEN
	}
	
	private long brokenTS;
	private STATE state = STATE.VALID;
	
	public long getBrokenTime() {
		if (state == STATE.BROKEN) {
			return System.currentTimeMillis() - brokenTS;
		}
		return -1;
	}
	
	public void setState(STATE state) {
		if (state != this.state && state == STATE.BROKEN) {
			brokenTS = System.currentTimeMillis();
		}
		this.state = state;
	}
	
	
	public STATE getState() {
		return state;
	}
	
	
}
