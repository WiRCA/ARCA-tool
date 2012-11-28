/*
 * Copyright (C) 2012 by Eero Laukkanen, Risto Virtanen, Jussi Patana, Juha Viljanen,
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
import models.RCACase;
import models.User;
import models.events.Event;
import play.Logger;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * @author Risto Virtanen
 */
@With({LanguageController.class})
public class PublicRCACaseController extends Controller {
	
	/**
	 * Shows the RCA case with the given URLHash.
	 * User has to have rights to view the case.
	 * @param URLHash URLHash string of the RCA case
	 */
	public static void show(String URLHash) {
		RCACase rcaCase = RCACase.getRCACase(URLHash);
		notFoundIfNull(rcaCase);
		rcaCase = checkIfCurrentUserHasRightsForRCACase(rcaCase.id);
		Long lastMessage = rcaCase.getCauseStream().lastEvent;
		User currentUser = SecurityController.getCurrentUser();
		render(rcaCase, lastMessage, currentUser);
	}


	/**
	 * Publishes new events to clients that are viewing the case.
	 * @param URLHash URL hash of the RCA case
	 * @param lastReceived Last sent event for the client.
	 */
	public static void waitMessages(String URLHash, Long lastReceived) {
		RCACase rcaCase = RCACase.getRCACase(URLHash);
		notFoundIfNull(rcaCase);
		rcaCase = checkIfCurrentUserHasRightsForRCACase(rcaCase.id);
		notFoundIfNull(lastReceived);
		List<F.IndexedEvent<Event>> messages = await(rcaCase.nextMessages(lastReceived));
		rcaCase.getCauseStream().lastEvent = messages.get(messages.size() - 1).id;
		renderJSON(messages, new TypeToken<List<F.IndexedEvent<Event>>>(){}.getType());
	}


	/**
	 * Check if the current user has rights for a specific RCA case
	 * @todo refactor (return value)
	 * @param rcaCaseId The ID of the RCA case to be checked
	 * @return the RCA case that the user has access to
	 */
	public static RCACase checkIfCurrentUserHasRightsForRCACase(Long rcaCaseId) {
		RCACase rcaCase = RCACase.findById(rcaCaseId);
		notFoundIfNull(rcaCase);

		User user = SecurityController.getCurrentUser();
		if (!rcaCase.isCasePublic && (user == null || !user.caseIds.contains(rcaCase.id))) {
			if (user == null) {
				flash.put("url", request.url);
				try {
					Secure.login();
				} catch (Throwable throwable) {
					Logger.error("Login failed.");
				}
				return null;
			} else if (!user.caseIds.contains(rcaCase.id)) {
				forbidden();
				return null;
			}
		}
		return rcaCase;
	}
}
