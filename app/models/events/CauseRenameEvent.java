package models.events;

import models.Cause;

/**
 * Rename event of the cause
 * @author Juha Viljanen
 */
public class CauseRenameEvent extends Event {

	/**
	* the id of the cause to be renamed
	*/
	public final String causeId;
	/**
	* the new name of the case
	*/
	public final String newName;

	/**
	* Basic constructor
	*/
	public CauseRenameEvent(Long causeId, String newName) {
		super("causeRenameEvent");
		this.newName = newName.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;");
		this.causeId = causeId.toString();
	}
	
}
