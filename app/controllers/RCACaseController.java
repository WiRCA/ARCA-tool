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

import com.google.gson.reflect.TypeToken;
import models.Invitation;
import models.RCACase;
import models.User;
import models.enums.CompanySize;
import models.enums.RCACaseType;
import notifiers.Mails;
import play.Logger;
import play.data.validation.Email;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.*;

import play.libs.F.*;
import models.events.*;

/**
 * Methods related to RCA cases.
 * @author Mikko Valjus
 * @author Risto Virtanen
 * @author Juha Viljanen
 */
@With({LanguageController.class})
public class RCACaseController extends Controller {

	private static final String FIND_BY_EMAIL = "byEmail";

	/**
	 * Checks whether the user is logged in.
	 * Users that are not logged in can view and contribute to public RCA cases and get their events.
	 * @throws Throwable
	 */
	@Before(unless={"show", "waitMessages"})
    static void checkAccess() throws Throwable {
        Secure.checkAccess();
    }

	/**
	 * Opens the new RCA case creation form.
	 */
	public static void createRCACase() {
		RCACase rcaCase = new RCACase(SecurityController.getCurrentUser());
		RCACaseType[] types = RCACaseType.values();
		CompanySize[] companySizes = CompanySize.values();
		render(rcaCase, types, companySizes);
	}

	/**
	 * Creates a new RCA case with the values given in the RCA case creation form.
	 * @param rcaCase
	 */
	public static void create(@Valid RCACase rcaCase) {
		if (validation.hasErrors()) {
			params.flash(); // add http parameters to the flash scope
			validation.keep(); // keep the errors for the next request
			createRCACase();
		}
		rcaCase.save();
		User user = SecurityController.getCurrentUser();
		user.addRCACase(rcaCase);
		Logger.info("User %s created new RCA case with name %s", user, rcaCase.caseName);
		show(rcaCase.id);
	}

	/**
	 * Shows the RCA case with the given id.
	 * User has to have rights to view the case.
	 * @param id
	 */
	public static void show(Long id) {
		RCACase rcaCase = RCACase.findById(id);
		checkIfCurrentUserHasRightsForRCACase(rcaCase);
		String size = rcaCase.getCompanySize().text;
		String type = rcaCase.getRCACaseType().text;
		Long lastMessage = 0L;
		List<IndexedEvent> archivedEvents = rcaCase.getCauseStream().eventStream.availableEvents(0);
		if (archivedEvents.size() > 0) {
			IndexedEvent last = archivedEvents.get(archivedEvents.size() - 1);
			lastMessage = last.id;
		}
		User currentUser = SecurityController.getCurrentUser();
		render(rcaCase, type, size, lastMessage, currentUser);
	}

	/**
	 * Publishes new events to clients that are viewing the case.
	 * @param rcaCaseId
	 * @param lastReceived Last sent event for the client.
	 */
	public static void waitMessages(Long rcaCaseId, Long lastReceived) {
		RCACase rcaCase = RCACase.findById(rcaCaseId);
		checkIfCurrentUserHasRightsForRCACase(rcaCase);
		User currentUser = SecurityController.getCurrentUser();
		Logger.info("Sending case %s events to user %s", rcaCase, currentUser);
		List messages = await(rcaCase.nextMessages(lastReceived));
		renderJSON(messages, new TypeToken<List<IndexedEvent<Event>>>() {
		}.getType());
	}

	/**
	 * Views users that have access to the RCA case.
	 * @param rcaCaseId
	 */
	public static void getUsers(Long rcaCaseId) {
		RCACase rcaCase = RCACase.findById(rcaCaseId);
		checkIfCurrentUserHasRightsForRCACase(rcaCase);
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
	 * @param rcaCaseId
	 * @param invitedEmail
	 */
	public static void inviteUser(Long rcaCaseId, @Required @Email String invitedEmail) {
		User current = SecurityController.getCurrentUser();
		RCACase rcaCase = RCACase.findById(rcaCaseId);
		checkIfCurrentUserHasRightsForRCACase(rcaCase);
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
				invitedUser.addRCACase(rcaCase);
				render(invitedUser);
			} else {
				Invitation invitation = Invitation.find(FIND_BY_EMAIL, invitedEmail).first();
				if (invitation == null) {
					invitation = new Invitation(invitedEmail).save();
					Mails.invite(current, invitation, rcaCase);
				}
				invitation.addCase(rcaCase);
				render(invitation);
			}
		}
	}

	/**
	 * Removes a user from the RCA case.
	 * Only the RCA case owner can remove users from his case.
	 * @param rcaCaseId
	 * @param isInvitedUser
	 * @param email
	 */
	public static void removeUser(Long rcaCaseId, Boolean isInvitedUser, String email) {
		User current = SecurityController.getCurrentUser();
		RCACase rcaCase = RCACase.findById(rcaCaseId);
		checkIfCurrentUserHasRightsForRCACase(rcaCase);
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
	 * @param rcaCaseId
	 */
	public static void extractCSV(Long rcaCaseId) {
		RCACase rcaCase = RCACase.findById(rcaCaseId);
		checkIfCurrentUserHasRightsForRCACase(rcaCase);
		response.setHeader("Content-Disposition", "attachment;filename=" + rcaCase.caseName.replace(" ",
		                                                                                            "-") + ".csv");
		request.format = "text/csv";
		renderTemplate("RCACaseController/extractCSV.csv", rcaCase);
	}

	private static void checkIfCurrentUserHasRightsForRCACase(RCACase rcaCase) {
		notFoundIfNull(rcaCase);

		User user = SecurityController.getCurrentUser();
		if (rcaCase.isCasePublic) {
			return;
		}
		else if (user == null || !user.caseIds.contains(rcaCase.id) ) {
			forbidden();
		}
	}

}
