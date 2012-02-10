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

import models.Cause;
import models.Correction;
import models.RCACase;
import models.User;
import models.events.*;
import play.Logger;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;

/**
 * Methods related to causes.
 *
 * @author Eero Laukkanen
 * @author Juha Viljanen
 */

@With({LanguageController.class})
public class CauseController extends Controller {

	/**
	 * Check that user has rights to edit the cause.
	 *
	 * @param causeId causeId that will be edited
	 */
	@Before
	public static void checkAuthentication(Long causeId) {
		validation.required(causeId);
		if (Validation.hasErrors()) {
			forbidden();
		}
		Cause cause = Cause.findById(causeId);
		PublicRCACaseController.checkIfCurrentUserHasRightsForRCACase(cause.rcaCase.id);
	}

	/**
	 * Adds a sub-cause to a cause.
	 *
	 * @param causeId
	 * @param name
	 */
	public static void addCause(Long causeId, String name) {
		// causeId is used later as a String
		Cause cause = Cause.findById(causeId);
		RCACase rcaCase = cause.rcaCase;

		Cause newCause = cause.addCause(name, SecurityController.getCurrentUser());

		AddCauseEvent event = new AddCauseEvent(newCause, causeId);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.debug("Cause %s added to cause %s", name, cause);
	}

	/**
	 * Adds a relation between causes.
	 *
	 * @param causeId
	 * @param toId
	 */
	public static void addRelation(Long causeId, Long toId) {
		Cause causeFrom = Cause.findById(causeId);
		RCACase rcaCase = causeFrom.rcaCase;

		Cause causeTo = Cause.findById(toId);

		causeFrom.addCause(causeTo);

		AddRelationEvent event = new AddRelationEvent(causeId, toId);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.debug("Relation added between %s and %s", causeFrom, causeTo);
	}
	
	/**
	 * Gets the names of the corrective actions of a cause.
	 *
	 * @param causeId
	 *
	 * @return
	 */
	public static void getCorrectionNames(Long causeId) {
		Cause cause = Cause.findById(causeId);
    ArrayList<String> listOfNames = new ArrayList<String>();
    for (Correction correction : cause.corrections) {
      listOfNames.add(correction.name);
    }
		renderJSON(listOfNames);
	}

	/**
	 * Adds a corrective action for a cause.
	 *
	 * @param causeId
	 * @param name
	 */
	public static void addCorrection(Long causeId, String name, String description) {
		Cause causeTo = Cause.findById(causeId);
		RCACase rcaCase = causeTo.rcaCase;

		causeTo.addCorrection(name, description);
		causeTo.save();

		AddCorrectionEvent event = new AddCorrectionEvent(causeTo, name, description);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.debug("Correction added to cause %s with description %s", causeTo.name, description);
	}

	/**
	 * Deletes the given cause, if the user has the permission to delete it.
	 * The root node of an RCA case can not be deleted.
	 * RCA case owner can delete all other nodes.
	 * Other users can only delete nodes that they have created themselves.
	 *
	 * @param causeId
	 */
	public static void deleteCause(Long causeId) {
		Cause cause = Cause.findById(causeId);
		RCACase rcaCase = cause.rcaCase;

		if (!CauseController.userIsAllowedToDelete(cause, rcaCase)) {
			//TODO: notify user that she cannot remove the problem cause
			return;
		}

		rcaCase.deleteCause(cause);

		DeleteCauseEvent deleteEvent = new DeleteCauseEvent(cause);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(deleteEvent);
		Logger.debug("Cause %s deleted", cause);
	}

	private static boolean userIsAllowedToDelete(Cause cause, RCACase rcaCase) {
		User current = SecurityController.getCurrentUser();
		return current != null && !cause.equals(rcaCase.problem) &&
		       (current == cause.getCreator() || current == rcaCase.getOwner());
	}

	/**
	 * Send an event that a node has moved.
	 *
	 * @param causeId id of the node that has moved
	 * @param x       new relative x coordinate of the node
	 * @param y       new relative y coordinate of the node
	 */
	public static void nodeMoved(Long causeId, int x, int y) {
		Cause cause = Cause.findById(causeId);
		RCACase rcaCase = cause.rcaCase;

		cause.xCoordinate = x;
		cause.yCoordinate = y;
		
		cause.save();

		NodeMovedEvent movedEvent = new NodeMovedEvent(cause, x, y);

		CauseStream causeStream = rcaCase.getCauseStream();
		causeStream.getStream().publish(movedEvent);
		Logger.debug("Cause %s moved to x:%d, y:%d", cause, x, y);
	}

	/**
	 * Adds a like to the cause if user has rights to do it.
	 * @param causeId id of the cause to be liked
	 */
	public static void like(Long causeId) {
		Cause cause = Cause.findById(causeId);
		RCACase rcaCase = cause.rcaCase;
		User user = SecurityController.getCurrentUser();
		if (user == null) {
			forbidden();
		}
		if (rcaCase.getOwner().equals(user)) {
			cause.like(user);	
		} else if (!cause.hasUserLiked(user)) {
			cause.like(user);	
		}
		Logger.debug("Cause %s liked by %s", cause, user);
		
		String likeData = String.format("{\"count\":%d,\"hasliked\":%b,\"isowner\":%b}", cause.countLikes(), 
		                                cause.hasUserLiked(user), user.equals(rcaCase.getOwner()));
		
		renderJSON(likeData);
	}

	/**
	 * Removes a like from the cause.
	 * @param causeId id of the cause to be disliked
	 */
	public static void dislike(Long causeId) {
		Cause cause = Cause.findById(causeId);
		RCACase rcaCase = cause.rcaCase;
		User user = SecurityController.getCurrentUser();
		if (user == null) {
			forbidden();
		}
		cause.dislike(user);
		Logger.debug("Cause %s disliked by %s", cause, user);
		String likeData = String.format("{\"count\":%d,\"hasliked\":%b,\"isowner\":%b}", cause.countLikes(),
		                                cause.hasUserLiked(user), user.equals(rcaCase.getOwner()));

		renderJSON(likeData);
	}

	/**
	 * Adds a sub-cause to a cause.
	 *
	 * @param causeId
	 * @param name
	 */
	public static void renameCause(Long causeId, String name) {
		// causeId is used later as a String
		Cause cause = Cause.findById(causeId);
		RCACase rcaCase = cause.rcaCase;

		String oldName = cause.name;
		
		cause.name = name;
		cause.save();
		
		if(cause == rcaCase.problem) {
			rcaCase.caseName = name;
			rcaCase.save();
		}

		CauseRenameEvent event = new CauseRenameEvent(causeId, name);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.debug("Cause %s renamed to cause %s", oldName, name);
	}
}
