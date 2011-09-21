package org.jsoftware.fods.event;


public class IndexChangedChangeEvent extends AbstractChangeEvent {
	private int fromIndex;
	private boolean userRequest;
	
	public IndexChangedChangeEvent(int newIndex, int fromIndex) {
		super(newIndex);
		this.fromIndex = fromIndex;
	}
	
	public boolean isUserRequest() {
		return userRequest;
	}
	
	public void setUserRequest(boolean userRequest) {
		this.userRequest = userRequest;
	}
	
	public int getFromIndex() {
		return fromIndex;
	}
	
	public int getToIndex() {
		return getIndex();
	}
		
	
	@Override
	public String toString() {
		String ur = userRequest ? ",userRequest" : "";
		return getClass().getSimpleName() + "(index="+fromIndex+" to " + getIndex() + ")" + ur;
	}

	private static final long serialVersionUID = -5168363946234877658L;

}
