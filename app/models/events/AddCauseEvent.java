package models.events;

import models.Cause;

public class AddCauseEvent extends Event {

	public final String user;
	public final String text;

	public AddCauseEvent(Cause cause) {
		super("addcauseevent");
		this.user = cause.getCreator().toString();
		this.text = cause.name;
	}

}



