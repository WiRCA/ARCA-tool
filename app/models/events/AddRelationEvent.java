package models.events;

import models.Cause;

public class AddRelationEvent extends Event {

	public final String causeFrom;
	public final String causeTo;

	public AddRelationEvent(String causeFrom, String causeTo) {
		super("addrelationevent");
		this.causeTo = causeTo;
		this.causeFrom = causeFrom;
	}

}