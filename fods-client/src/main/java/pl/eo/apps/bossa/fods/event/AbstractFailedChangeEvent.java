package pl.eo.apps.bossa.fods.event;

public class AbstractFailedChangeEvent extends AbstractChangeEvent {
	private Throwable reason;
	
	public AbstractFailedChangeEvent(int index, Throwable reason) {
		super(index);
		this.reason = reason;
	}
	
	public Throwable getReason() {
		return reason;
	}

	private static final long serialVersionUID = 7135297855355440977L;

}
