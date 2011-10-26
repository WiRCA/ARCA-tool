package controllers;

import models.User;
import play.Logger;
import utils.EncodingUtils;

import java.security.NoSuchAlgorithmException;

/**
 * @author Risto Virtanen
 */
public class SecurityController extends Secure.Security {

	static boolean authenticate(String username, String password) {
		try {
			return User.find("byEmailAndPassword", username, EncodingUtils.encodeSHA1(password)).first() != null;
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e.getMessage(), "User authentication failed");
			return false;
		}
	}
}
