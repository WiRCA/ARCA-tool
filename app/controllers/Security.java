package controllers;

/**
 * @author Risto Virtanen
 */
public class Security extends Secure.Security {

	static boolean authenticate(String username, String password) {
		// TODO check user from database, if exists check password, if matches return true
		return true;
	}
}
