package models.events;

import models.Cause;

public class AddCauseEvent extends Event {

	public final String user;
	public final String text;
	public final String causeFrom;
	public final String causeTo;

	public AddCauseEvent(Cause cause, String causeFrom) {
		super("addcauseevent");
		this.causeTo = Long.toString(cause.id);
		this.user = cause.getCreator().toString();
		this.text = cause.name;
		this.causeFrom = causeFrom;
	}

}



