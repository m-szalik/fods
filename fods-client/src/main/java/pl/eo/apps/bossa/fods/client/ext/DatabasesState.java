package pl.eo.apps.bossa.fods.client.ext;

public class DatabasesState {
	private int size;
	private Throwable[] breakReasons;
	
	
	public DatabasesState(int size) {
		this.size = size;
		this.breakReasons = new Throwable[size];
	}
	
	public void setReakReason(int index, Throwable reason) {
		breakReasons[index] = reason;
	}
	
	public boolean isBroken(int index) {
		return breakReasons[index] != null;
	}
	
	public void clearReason(int index) {
		setReakReason(index, null);
	}
	
	public int size() {
		return size;
	}
	
	
}
