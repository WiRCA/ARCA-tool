package models.enums;

import play.i18n.Messages;

/**
 * @author Juha Viljanen
 */
public enum CompanySize {

	TEN(1, "1-10"),
	FIFTY(2, "11-50"),
	HUNDRED(3, "51-100");

	public int value;
	public String text;

	CompanySize(int value, String text) {
		this.value = value;
		this.text = text;
	}

	public static CompanySize valueOf(int id) {
		for (CompanySize size : CompanySize.values()) {
			if (size.value == id) {
				return size;
			}
		}
		return null;
	}
}
