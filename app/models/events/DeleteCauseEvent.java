package models.events;

import models.Cause;

public class DeleteCauseEvent extends Event {

	public final String user;
	public final String text;
	public final String causeFrom;

	public DeleteCauseEvent(Cause cause, String causeFrom) {
		super("deletecauseevent");
		//TODO
		this.user = cause.getCreator().toString();
		this.text = cause.name;
		this.causeFrom = causeFrom;
	}

}



