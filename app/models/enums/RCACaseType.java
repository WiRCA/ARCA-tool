package models.enums;

import play.i18n.Messages;

/**
 * @author Juha Viljanen
 */
public enum RCACaseType {

	SOFT(1, Messages.get("RCACaseType.softwareProject")),
	HR(2, Messages.get("RCACaseType.hr")),
	OTHER(3, Messages.get("RCACaseType.other"));

	public int value;
	public String text;

	RCACaseType(int value, String text) {
		this.value = value;
		this.text = text;
	}

	public static RCACaseType valueOf(int id) {
		for (RCACaseType caseType : RCACaseType.values()) {
			if (caseType.value == id) {
				return caseType;
			}
		}
		return null;
	}
}
