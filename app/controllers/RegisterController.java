/*
 * Copyright (C) 2011 - 2013 by Eero Laukkanen, Risto Virtanen, Jussi Patana,
 * Juha Viljanen, Joona Koistinen, Pekka Rihtniemi, Mika Kekäle, Roope Hovi,
 * Mikko Valjus, Timo Lehtinen, Jaakko Harjuhahto, Jonne Viitanen, Jari Jaanto,
 * Toni Sevenius, Anssi Matti Helin, Jerome Saarinen, Markus Kere
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import job.TutorialRCACaseJob;
import models.Invitation;
import models.RCACase;
import models.User;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.With;
import utils.EncodingUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Methods related to user registration.
 * @author Juha Viljanen
 * @author Risto Virtanen
 */
@With(LanguageController.class)
public class RegisterController extends Controller {

	private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";

	/**
	 * Opens page containing user registration form.
	 */
	public static void registerUser() {
		render();
	}

	/**
	 * Registers user with values given in the user registration form.
	 * An email can only be registered once.
	 * @param user User to be registered
	 * @param password2 second password field for validating the password
	 * @param invitationId id of the invitation if registering with invitation
	 * @param rcaCaseId id of the rca case if registering with invitation
	 */
	public static void register(@Valid User user, @Required String password2, Long invitationId, Long rcaCaseId) {

		Invitation invitation = null;
		if (invitationId != null) {
			invitation = Invitation.findById(invitationId);
			notFoundIfNull(invitation);
			if (!invitation.email.equals(user.email)) {
				renderTemplate("RegisterController/registerUser.html", invitation, rcaCaseId);
			}
		} else {
			validateUserIsNotInvited(user.email);
		}
		validateUserDoesNotExist(user.email);
		validation.equals(user.password, password2).key("user.password").message("register.passwordsDidNotMatch");

		if (validation.hasErrors()) {
			params.flash(); // add http parameters to the flash scope
			if (invitation != null) {
				renderTemplate("RegisterController/registerUser.html", invitation, rcaCaseId);
			}
			validation.keep(); // keep the errors for the next request
			registerUser();
		}

		addCaseAndDeleteInvitationIfInvited(invitation, user);

		user.changePassword(password2);
		user.save();

		new TutorialRCACaseJob().doJob(user, false);

		Logger.info("User %s registered", user);

        // Mark user as connected
        session.put("username", user.email);

		showCaseIfInvited(rcaCaseId, user);

		IndexPageController.index();
	}

	public static void googlePlus(String googleAccessToken, String authenticityToken) throws NoSuchAlgorithmException {
		checkAuthenticity();
		String apiUrl = "https://www.googleapis.com/plus/v1/people/me?access_token=" + googleAccessToken;
		final JsonObject jsonResponse = WS.url(apiUrl).get().getJson().getAsJsonObject();
		final String fullName = jsonResponse.get("displayName").getAsString();
		JsonElement emails = jsonResponse.get("emails");
		String email = null;
		for (JsonElement emailElement : emails.getAsJsonArray()) {
			final JsonObject emailObject = (JsonObject) emailElement;
			if ("account".equals(emailObject.get("type").getAsString())) {
				email = emailObject.get("value").getAsString();
				break;
			}
		}

		if (!userExists(email)) {
			createGoogleUser(email, fullName);
		}
		session.put("username", email);
		SecurityController.onAuthenticated();
	}

	/**
	 * Redirects a invited user to the registration form.
	 * The email address is already given in the registration form and cannot be changed.
	 * @param invitationId id of the invitation
	 * @param rcaCaseId id of a rca case to which the user is invited to
	 * @param inviteHash hash of the invitation to recognize the user
	 */
	public static void registerInvitation(Long invitationId, Long rcaCaseId, String inviteHash) {
		if (SecurityController.isConnected()) {
			IndexPageController.index();
		}
		notFoundIfNull(invitationId);
		notFoundIfNull(rcaCaseId);
		notFoundIfNull(inviteHash);
		Invitation invitation = Invitation.findById(invitationId);
		notFoundIfNull(invitation);
		if (!inviteHash.equals(EncodingUtils.encodeSHA1Base64(invitation.hash))) {
			forbidden();
		}
		renderTemplate("RegisterController/registerUser.html", invitation, rcaCaseId);
	}

	private static void createGoogleUser(String email, String name) throws NoSuchAlgorithmException {
		// random password is generated for the user object, since it is required in the database
		SecureRandom secureRandom = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM);
		String randomPasswd = Integer.toHexString(secureRandom.nextInt());

		User user = new User(email, randomPasswd);
		user.name = name;

		Invitation invitation = Invitation.find("byEmail", email).first();

		addCaseAndDeleteInvitationIfInvited(invitation, user);

		user.save();
		new TutorialRCACaseJob().doJob(user, false);
		Logger.info("User %s registered via Google login", user);
	}

	private static boolean userExists(String email) {
		return User.find("byEmail", email).first() != null;
	}

	private static boolean userIsInvited(String email) {
		return Invitation.find("byEmail", email).first() != null;
	}

	private static void validateUserDoesNotExist(String email) {
		doValidationAndGiveMessage(!userExists(email));
	}

	private static void validateUserIsNotInvited(String email) {
		doValidationAndGiveMessage(!userIsInvited(email));
	}

	private static void doValidationAndGiveMessage(boolean conditionToValidate) {
		validation.isTrue(conditionToValidate).key("user.email").message("register.emailExists");
	}

	private static void addCaseAndDeleteInvitationIfInvited(Invitation invitation, User user) {
		if (invitation != null) {
			user.caseIds.addAll(invitation.caseIds);
			invitation.delete();
		}
	}

	private static void showCaseIfInvited(Long rcaCaseId, User user) {
		if (rcaCaseId != null && user.caseIds.contains(rcaCaseId)) {
			RCACase rcaCase = RCACase.findById(rcaCaseId);
			PublicRCACaseController.show(rcaCase.URLHash);
		}
	}
}
