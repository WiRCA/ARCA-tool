package controllers;

import models.User;
import utils.EncodingUtils;

/**
 * @author Risto Virtanen
 */
public class SecurityController extends Secure.Security {

	public static boolean authenticate(String username, String password) {
		User found = User.find("byEmailAndPassword", username, EncodingUtils.encodeSHA1(password)).first();
		if (found != null) {
			session.put("userFullname", found.name);
		}
		return found != null;
	}

	public static void onAuthenticated() {
		UserController.index();
	}

	public static void onDisconnected() {
		ApplicationController.index();
	}

	public static User getCurrentUser() {
		return User.find("byEmail", SecurityController.connected()).first();
	}
}
