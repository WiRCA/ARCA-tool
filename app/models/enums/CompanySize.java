package models.enums;

import play.i18n.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: juha
 * Date: 11/4/11
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public enum CompanySize {

	TEN(1, "1-10"),
	FIFTY(2, "11-50"),
	HUNDRED(2, "51-100");

	public int value;
	public String text;

	CompanySize(int value, String text) {
		this.value = value;
		this.text = text;
	}
}
