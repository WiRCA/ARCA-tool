package models;

/**
 * @author Juha Viljanen
 */
public class MandatoryFieldEmptyException extends Exception {
	public MandatoryFieldEmptyException(String msg) {
		super(msg);
	}
}
