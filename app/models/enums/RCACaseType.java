package models.enums;

import play.i18n.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: juha
 * Date: 11/4/11
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public enum RCACaseType {

	SOFT(1, Messages.get("RCACaseType.softwareProject")),
	HR(2, Messages.get("RCACaseType.hr")),
	OTHER(2, Messages.get("RCACaseType.other"));

	public int value;
	public String text;

	RCACaseType(int value, String text) {
		this.value = value;
		this.text = text;
	}
}
