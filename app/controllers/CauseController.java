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

import models.*;
import models.events.*;
import play.Logger;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Set;

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
	 * @param causeId id of the cause to which the new cause is added
	 * @param name the name of the new cause
	 * @param classificationId1 the ID of the first classification
	 * @param classificationId2 the ID of the second classification
	 */
	public static void addCause(Long causeId, String name, Long classificationId1, Long classificationId2) {
		// causeId is used later as a String
		Cause cause = Cause.findById(causeId);
		RCACase rcaCase = cause.rcaCase;

		Classification classification1 = null, classification2 = null;
		if (classificationId1 != -1) {
			classification1 = Classification.findById(classificationId1);
			if (classification1 == null) { error(); }
		}
		if (classificationId2 != -1) {
			classification2 = Classification.findById(classificationId2);
			if (classification2 == null) { error(); }
		}

		Cause newCause = cause.addCause(name, SecurityController.getCurrentUser());

		if (classification1 != null) { newCause.setClassification(classification1); }
		if (classification2 != null) { newCause.setClassification(classification2); }

		AddCauseEvent event = new AddCauseEvent(newCause, causeId, classificationId1, classificationId2);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.debug("Cause %s added to cause %s with classifications %d and %d",
		             name, cause, classificationId1, classificationId2);
	}

	/**
	 * Adds a relation between causes.
	 *
	 * @param causeId relation from cause id
	 * @param toId relation to cause id
	 */
	public static void addRelation(Long causeId, Long toId) {
		Cause causeFrom = Cause.findById(causeId);
		RCACase rcaCase = causeFrom.rcaCase;

		Cause causeTo = Cause.findById(toId);

		causeTo.addCause(causeFrom);

		AddRelationEvent event = new AddRelationEvent(causeId, toId);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.debug("Relation added between %s and %s", causeFrom, causeTo);
	}
	
	/**
	 * Gets the corrective actions of a cause and renders them in a variable.
	 *
	 * @param causeId corrrective actions for cause id
	 */
	public static void getCorrections(Long causeId) {
		Cause cause = Cause.findById(causeId);
		Set<Correction> listOfCorrections = cause.corrections;
		User user = SecurityController.getCurrentUser();

		render(listOfCorrections, user);
	}

	/**
	 * Adds a corrective action for a cause.
	 *
	 * @param causeId cause's id to add the new corrective action
	 * @param name name of the corrective action
	 * @param description description of the corrective action
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
	 * @param causeId delete cause id
	 */
	public static void deleteCause(Long causeId) {
		Cause cause = Cause.findById(causeId);
		RCACase rcaCase = cause.rcaCase;

		if (!CauseController.userIsAllowedToDeleteAndRename(cause, rcaCase)) {
			forbidden();
		}

		rcaCase.deleteCause(cause);

		DeleteCauseEvent deleteEvent = new DeleteCauseEvent(cause);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(deleteEvent);
		Logger.debug("Cause %s deleted", cause);
	}

	private static boolean userIsAllowedToDeleteAndRename(Cause cause, RCACase rcaCase) {
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
		
		if (!userAllowedToLike(user, rcaCase, cause)) {
			forbidden();
		}

		cause.like(user);

		AmountOfLikesEvent event = new AmountOfLikesEvent(causeId, cause.countLikes());
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);

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

		if (!userAllowedToDislike(user, rcaCase, cause)) {
			forbidden();
		}
		cause.dislike(user);

		AmountOfLikesEvent event = new AmountOfLikesEvent(causeId, cause.countLikes());
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);

		Logger.debug("Cause %s disliked by %s", cause, user);

		String likeData = String.format("{\"count\":%d,\"hasliked\":%b,\"isowner\":%b}", cause.countLikes(),
            cause.hasUserLiked(user), user.equals(rcaCase.getOwner()));
		renderJSON(likeData);
	}

	/**
	 * Renames a cause.
	 * @param causeId rename cause with id
	 * @param name new name of the cause
	 */
	public static void renameCause(Long causeId, String name) {
		// causeId is used later as a String
		Cause cause = Cause.findById(causeId);
		RCACase rcaCase = cause.rcaCase;

        if (!CauseController.userIsAllowedToDeleteAndRename(cause, rcaCase)) {
            forbidden();
        }
		String oldName = cause.name;
		
		cause.name = name;
		cause.save();
		
		if (cause == rcaCase.problem) {
			rcaCase.caseName = name;
			rcaCase.save();
		}

		CauseRenameEvent event = new CauseRenameEvent(causeId, name);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.debug("Cause %s renamed to cause %s", oldName, name);
	}


	/**
	 * A quick dual-classification setting function for the prototype.
	 * @todo Make this properly :)
	 * @param causeId the ID of the cause
	 * @param classificationId1 the ID of the first classification to set
	 * @param classificationId2 the ID of the second classification to set
	 */
	public static void setClassifications(Long causeId, Long classificationId1, Long classificationId2) {
		Cause cause = Cause.findById(causeId);
		RCACase rcaCase = cause.rcaCase;

		if (!CauseController.userAllowedToClassify(cause)) {
			forbidden();
		}

		CauseStream causeEvents = rcaCase.getCauseStream();
		if (classificationId1 != -1) {
			Classification classification1 = Classification.findById(classificationId1);
			if (classification1 == null) { error(); return; }
			cause.setClassification(classification1);
			cause.save();
			CauseClassificationEvent event1 = new CauseClassificationEvent(causeId, classificationId1);
			causeEvents.getStream().publish(event1);
			Logger.debug("Case %s reclassified to %s", cause.name, classification1.name);
		}

		if (classificationId2 != -1) {
			Classification classification2 = Classification.findById(classificationId2);
			if (classification2 == null) { error(); return; }
			cause.setClassification(classification2);
			cause.save();
			CauseClassificationEvent event2 = new CauseClassificationEvent(causeId, classificationId2);
			causeEvents.getStream().publish(event2);
			Logger.debug("Case %s reclassified to %s", cause.name, classification2.name);
		}
	}


	private static boolean userAllowedToClassify(Cause cause) {
		// TODO: Who is actually allowed to (re)classify causes?
		User current = SecurityController.getCurrentUser();
		return current != null;
	}


	private static boolean userAllowedToLike(User user, RCACase rcaCase, Cause cause) {
		return user != null && (rcaCase.getOwner().equals(user) || !cause.hasUserLiked(user));		
	}
	
	private static boolean userAllowedToDislike(User user, RCACase rcaCase, Cause cause) {
		return (cause.countLikes() > 0 && user != null &&
		        (rcaCase.getOwner().equals(user) || cause.hasUserLiked(user)));
	}
}
