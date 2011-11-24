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
import play.mvc.Controller;
import play.mvc.With;

import java.util.*;

import play.libs.F.*;
import models.events.*;

/**
 * @author Mikko Valjus
 * @author Risto Virtanen
 */
@With({Secure.class, LanguageController.class})
public class RCACaseController extends Controller {

	public static void createRCACase() {
		RCACase rcaCase = new RCACase(SecurityController.getCurrentUser());
		RCACaseType[] types = RCACaseType.values();
		CompanySize[] companySizes = CompanySize.values();
		render(rcaCase, types, companySizes);
	}

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


	public static void show(Long id) {
		RCACase rcaCase = RCACase.findById(id);
		notFoundIfNull(rcaCase);
		String size = rcaCase.getCompanySize().text;
		String type = rcaCase.getRCACaseType().text;
		render(rcaCase, type, size);
	}

	public static void waitMessages(Long id, Long lastReceived) {
		RCACase rcaCase = RCACase.findById(id);
		notFoundIfNull(rcaCase);
		List messages = await(rcaCase.nextMessages(lastReceived));
		renderJSON(messages, new TypeToken<List<IndexedEvent<Event>>>() {
		}.getType());
	}

	public static void getUsers(Long rcaCaseId) {
		RCACase rcaCase = RCACase.findById(rcaCaseId);
		notFoundIfNull(rcaCase);
		List<User> existingUsers = User.find("Select u from user as u inner join u.caseIds as caseIds" +
		                             " where ? in caseIds", rcaCaseId).fetch();
		List<Invitation> invitedUsers = Invitation.find("Select iu from invitation as iu join iu.caseIds as caseIds" +
		                                                " where ? in caseIds", rcaCaseId).fetch();
		request.format = "json";
		render(existingUsers, invitedUsers);
	}

	public static void inviteUser(Long rcaCaseId, @Required @Email String invitedEmail) {
		User current = SecurityController.getCurrentUser();
		RCACase rcaCase = RCACase.findById(rcaCaseId);
		notFoundIfNull(rcaCase);
		if (invitedEmail == null || invitedEmail.isEmpty()) {
			notFound();
		}
		if (validation.hasErrors()) {
			renderJSON("{\"invalidEmail\":\"true\"}");
		}
		// Check if user the owner of the RCA case
		if (rcaCase.ownerId.equals(current.id)) {
			request.format = "json";
			User invitedUser = User.find("byEmail", invitedEmail).first();
			if (invitedUser != null) {
				invitedUser.addRCACase(rcaCase);
				render(invitedUser);
			} else {
				Invitation invitation = Invitation.find("byEmail", invitedEmail).first();
				if (invitation == null) {
					invitation = new Invitation(invitedEmail).save();
					Mails.invite(current, invitation, rcaCase);
				}
				invitation.addCase(rcaCase);
				render(invitation);
			}
		}
	}

	public static void removeUser(Long rcaCaseId, Boolean isInvitedUser, String email) {
		User current = SecurityController.getCurrentUser();
		RCACase rcaCase = RCACase.findById(rcaCaseId);
		notFoundIfNull(rcaCase);
		// Check if user the owner of the RCA case
		if (rcaCase.ownerId.equals(current.id)) {
			if (isInvitedUser) {
				Invitation invitation = Invitation.find("byEmail", email).first();
				notFoundIfNull(invitation);
				if (invitation.caseIds.contains(rcaCaseId)) {
					invitation.removeRCACase(rcaCase);
					Logger.info("User %s removed invitation for %s to RCA case %s", current, invitation, rcaCase);
					renderJSON("{\"success\":\"true\"}");
				}
			} else {
				User user = User.find("byEmail", email).first();
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

}
