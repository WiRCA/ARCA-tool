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

import models.*;
import models.enums.CompanySize;
import models.enums.RCACaseType;
import notifiers.Mails;
import play.Logger;
import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.libs.Codec;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Methods related to RCA cases.
 * @author Mikko Valjus
 * @author Risto Virtanen
 * @author Juha Viljanen
 */
@With({Secure.class, LanguageController.class})
public class RCACaseController extends Controller {

	private static final String FIND_BY_EMAIL = "byEmail";

	/**
	 * Checks whether the user is logged in.
	 * Users that are not logged in can view and contribute to public RCA cases and get their events.
	 * @throws Throwable Thrown from Play's Secure
	 */
	@Before(unless={"show", "waitMessages"})
    static void checkAccess() throws Throwable {
        Secure.checkAccess();
    }


	/**
	 * Opens the new RCA case creation form.
	 */
	public static void createRCACase() {
		RCACaseType[] types = RCACaseType.values();
		CompanySize[] companySizes = CompanySize.values();

		User user = SecurityController.getCurrentUser();
		if (user == null) {
			session.remove("username");
			redirect("/");
			return;
		}

		Set<RCACase> cases = user.getRCACases();

		render(types, companySizes, cases);
	}


	/**
	 * Creates a new RCA case with the values given in the RCA case creation form.
	 * @param rcaCase created RCA case
	 */
	public static void create(@Valid RCACase rcaCase, @MaxSize(255) String problemName) {
		if (validation.hasErrors()) {
			params.flash(); // add http parameters to the flash scope
			validation.keep(); // keep the errors for the next request
			createRCACase();
		}

		User user = SecurityController.getCurrentUser();

		// Import classifications from the selected case (rcaCase.importId)
		if (rcaCase.importId != null) {
			RCACase importCase = RCACase.findById(rcaCase.importId);
			if (importCase != null) {
				List<Classification> classifications = importCase.getClassifications();
				for (Classification importClassification: classifications) {
					Classification classification = new Classification(rcaCase, importClassification.name, user,
					                                                   importClassification.classificationDimension,
					                                                   importClassification.explanation,
					                                                   importClassification.abbreviation);
					classification.save();
				}
			}
		}
		rcaCase.ownerId = user.id;
		rcaCase.causes = new TreeSet<Cause>();
		rcaCase.URLHash = Codec.UUID();
		rcaCase.save();

		if (problemName.trim().length() == 0) {
			problemName = rcaCase.caseName;
		}
		user.addRCACase(rcaCase, problemName);
		Logger.info("User %s created new RCA case with name %s", user, rcaCase.caseName);
		PublicRCACaseController.show(rcaCase.URLHash);
	}


	/**
	 * Views users that have access to the RCA case.
	 * @param rcaCaseId ID of the RCA case
	 */
	public static void getUsers(Long rcaCaseId) {
		PublicRCACaseController.checkIfCurrentUserHasRightsForRCACase(rcaCaseId);
		List<User> existingUsers =
				User.find("Select u from user as u inner join u.caseIds as caseIds" + " where ? in caseIds",
				          rcaCaseId)
				    .fetch();
		List<Invitation> invitedUsers = Invitation
				.find("Select iu from invitation as iu join iu.caseIds as caseIds" + " where ? in caseIds", rcaCaseId)
				.fetch();
		request.format = "json";
		render(existingUsers, invitedUsers);
	}


	/**
	 * Invites the user with the given email address to the RCA case.
	 * Only the RCA case owner can invite new users.
	 * If the invited user has registered to the system, the case is added to his cases.
	 * Otherwise an invitation email is sent to the given email address.
	 * @param rcaCaseId ID of the RCA case
	 * @param invitedEmail email that is invited
	 */
	public static void inviteUser(Long rcaCaseId, @Required @Email String invitedEmail) {
		User current = SecurityController.getCurrentUser();
		RCACase rcaCase = PublicRCACaseController.checkIfCurrentUserHasRightsForRCACase(rcaCaseId);
		if (invitedEmail == null || invitedEmail.isEmpty()) {
			notFound();
		}
		if (validation.hasErrors()) {
			renderJSON("{\"invalidEmail\":\"true\"}");
		}
		// Check if user the owner of the RCA case
		if (rcaCase.ownerId.equals(current.id)) {
			request.format = "json";
			User invitedUser = User.find(FIND_BY_EMAIL, invitedEmail).first();
			if (invitedUser != null) {
				Logger.debug("User %s invited %s to RCA case %s", current, invitedUser, rcaCase);
				invitedUser.addRCACase(rcaCase);
				render(invitedUser);
			} else {
				Invitation invitation = Invitation.find(FIND_BY_EMAIL, invitedEmail).first();
				if (invitation == null) {
					invitation = new Invitation(invitedEmail).save();
					Mails.invite(current, invitation, rcaCase);
				}
				invitation.addCase(rcaCase);
				Logger.debug("User %s invited %s to RCA case %s", current, invitation, rcaCase);
				render(invitation);
			}
		}
	}


	/**
	 * Removes a user from the RCA case.
	 * Only the RCA case owner can remove users from his case.
	 * @param rcaCaseId ID of the RCA case
	 * @param isInvitedUser if the user is instance of Invitation
	 * @param email email of the user
	 */
	public static void removeUser(Long rcaCaseId, Boolean isInvitedUser, String email) {
		User current = SecurityController.getCurrentUser();
		RCACase rcaCase = PublicRCACaseController.checkIfCurrentUserHasRightsForRCACase(rcaCaseId);
		// Check if user the owner of the RCA case
		if (rcaCase.ownerId.equals(current.id)) {
			if (isInvitedUser) {
				Invitation invitation = Invitation.find(FIND_BY_EMAIL, email).first();
				notFoundIfNull(invitation);
				if (invitation.caseIds.contains(rcaCaseId)) {
					invitation.removeRCACase(rcaCase);
					Logger.info("User %s removed invitation for %s to RCA case %s", current, invitation, rcaCase);
					renderJSON("{\"success\":\"true\"}");
				}
			} else {
				User user = User.find(FIND_BY_EMAIL, email).first();
				notFoundIfNull(user);
				if (!rcaCase.ownerId.equals(user.id) && user.caseIds.contains(rcaCaseId)) {
					user.removeRCACase(rcaCase);
					Logger.info("User %s removed invitation for %s to RCA case %s", current, user, rcaCase);
					renderJSON("{\"success\":\"true\"}");
				}
			}
		}
		renderJSON("{\"success\":\"false\"}");
	}


	/**
	 * Exports the RCA case to csv format, that the user can download.
	 * @param URLHash URL hash of the RCA case
	 */
	public static void extractCSV(String URLHash) {
		RCACase rcaCase = RCACase.getRCACase(URLHash);
		notFoundIfNull(rcaCase);
		rcaCase = PublicRCACaseController.checkIfCurrentUserHasRightsForRCACase(rcaCase.id);
		response.setHeader("Content-Disposition", "attachment;filename=" +
		                                          rcaCase.caseName.replace(" ", "-") + ".csv");
		request.format = "text/csv";
		renderTemplate("RCACaseController/extractCSV.csv", rcaCase);
	}

}
