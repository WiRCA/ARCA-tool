package models.events;

import models.Cause;

/**
 * @author Juha Viljanen
 */
public class CauseRenameEvent extends Event {

	public final String causeId;
	public final String newName;

	public CauseRenameEvent(Long causeId, String newName) {
		super("causeRenameEvent");
		this.newName = newName.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;");
		this.causeId = causeId.toString();
	}
	
}
