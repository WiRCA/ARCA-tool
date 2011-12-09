/*
 * Copyright (C) 2011 by Eero Laukkanen, Risto Virtanen, Jussi Patana, Juha Viljanen,
 * Joona Koistinen, Pekka Rihtniemi, Mika Kekäle, Roope Hovi, Mikko Valjus,
 * Timo Lehtinen, Jaakko Harjuhahto
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package controllers;

import models.User;
import play.Logger;
import utils.EncodingUtils;

/**
 * Methods related to authentication.
 * @author Risto Virtanen
 */
public class SecurityController extends Secure.Security {

	/**
	 * Authenticates the log in attempt.
	 * Checks whether a user with the given email and password has been registered.
	 * @param username
	 * @param password
	 * @return
	 */
	public static boolean authenticate(String username, String password) {
		User found = User.find("byEmailAndPassword", username, EncodingUtils.encodeSHA1(password)).first();
		return found != null;
	}

	/**
	 * This method is called after a successful authentication.
	 */
	static void onAuthenticated() {
		User current = getCurrentUser();
		Logger.info("User %s logged in", current);
		UserController.index();
	}

	 /**
	 * This method is called before a user tries to sign off.
	 */
	static void onDisconnect() {
		User current = getCurrentUser();
		Logger.info("User %s logged out", current);
	}

	 /**
	 * This method is called after a successful sign off.
	 * You need to override this method if you wish to perform specific actions (eg. Record the time the user signed off)
	 */
	static void onDisconnected() {
		IndexPageController.index();
	}

	/**
	 * Returns the user that is currently logged in.
	 * @return
	 */
	public static User getCurrentUser() {
		return User.find("byEmail", SecurityController.connected()).first();
	}
}
