package controllers;

import models.User;
import play.Logger;
import utils.EncodingUtils;

import java.security.NoSuchAlgorithmException;

/**
 * @author Risto Virtanen
 */
public class SecurityController extends Secure.Security {

	public static boolean authenticate(String username, String password) {
		try {
			User found = User.find("byEmailAndPassword", username, EncodingUtils.encodeSHA1(password)).first();
			if (found != null) {
				session.put("userFullname", found.name);
			}
			return found != null;
		} catch (NoSuchAlgorithmException e) {
			Logger.error(e.getMessage(), "User authentication failed");
			return false;
		}
	}

	static void onAuthenticated() {
		ApplicationController.index();
	}

	static void onDisconnected() {
		ApplicationController.index();
	}
}
