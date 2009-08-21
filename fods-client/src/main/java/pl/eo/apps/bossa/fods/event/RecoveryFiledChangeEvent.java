package pl.eo.apps.bossa.fods.event;

public class RecoveryFiledChangeEvent extends AbstractFailedChangeEvent {

	public RecoveryFiledChangeEvent(int index, Throwable reason) {
		super(index, reason);
	}

	
	private static final long serialVersionUID = -6364783427859807196L;

}
