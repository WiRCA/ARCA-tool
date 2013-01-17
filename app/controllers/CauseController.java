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
import java.util.TreeSet;

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
	 */
	public static void addCause(Long causeId, String name) {
		// causeId is used later as a String
		Cause cause = Cause.findById(causeId);
		RCACase rcaCase = cause.rcaCase;

		Cause newCause = cause.addCause(name, SecurityController.getCurrentUser());

		AddCauseEvent event = new AddCauseEvent(newCause, causeId);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.info("Cause %s added to cause %s", name, cause);
	}

	/**
	 * Adds a relation between causes.
	 *
	 * @param causeId relation from cause id
	 * @param toId relation to cause id
	 */
	public static void addRelation(Long causeId, Long toId) {
		Cause causeFrom = Cause.findById(causeId);

		// Adding a relation from a cause to itself not only is illogical, but causes
		// rather significant bugs as well.
		if (causeId.equals(toId)) {
			Logger.warn("Tried to add a relation from " + causeFrom.name + " to itself");
			forbidden("No self-relations");
		}

		RCACase rcaCase = causeFrom.rcaCase;
		Cause causeTo = Cause.findById(toId);

		causeTo.addCause(causeFrom);

		AddRelationEvent event = new AddRelationEvent(causeId, toId);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.info("Relation added between %s and %s", causeFrom, causeTo);
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
		// Authentication is done via function checkAuthentication(long causeId) automatically

		Cause causeTo = Cause.findById(causeId);
		RCACase rcaCase = causeTo.rcaCase;

		causeTo.addCorrection(name, description);
		causeTo.save();

		AddCorrectionEvent event = new AddCorrectionEvent(causeTo, name, description);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.info("Correction added to cause %s: %s", causeTo.name, description);
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
			Logger.info("User %s tried to delete cause %s", SecurityController.getCurrentUser().name, cause.name);
			forbidden();
		}

		rcaCase.deleteCause(cause);

		DeleteCauseEvent deleteEvent = new DeleteCauseEvent(cause);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(deleteEvent);
		Logger.debug("Cause %s deleted", cause);
	}

	public static void deleteRelation(Long causeId, Long toId) {
		System.out.println("wtf");
		Cause fromCause = Cause.findById(causeId);
		Cause toCause = Cause.findById(toId);
		System.out.println(fromCause + " -> " + toCause);

		RCACase rcaCase = fromCause.rcaCase;
		Relation relation = Relation.findByCauses(fromCause, toCause);
		if (relation == null) {
			Logger.warn(
				"Attempting to remove a nonexistent relation from %s to %s",
			    fromCause.name, toCause.name
			);
			forbidden("Nonexistent relation");
			return;
		}
		relation.delete();

		DeleteRelationEvent deleteEvent = new DeleteRelationEvent(fromCause, toCause);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(deleteEvent);
		Logger.info("Relation from %s to %s deleted", fromCause.name, toCause.name);
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
			Logger.info("User %s tried to like cause %s", user.name, cause.name);
			forbidden();
		}

		cause.like(user);

		AmountOfLikesEvent event = new AmountOfLikesEvent(causeId, cause.countLikes());
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);

		Logger.info("Cause %s liked by %s", cause, user);
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
			Logger.info("User %s tried to dislike %s", user.name, cause.name);
			forbidden();
		}
		cause.dislike(user);

		AmountOfLikesEvent event = new AmountOfLikesEvent(causeId, cause.countLikes());
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);

		Logger.info("Cause %s disliked by %s", cause, user);

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
	        Logger.info("User %s tried to rename cause %s", SecurityController.getCurrentUser().name, cause.name);
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
		Logger.info("Cause %s renamed to cause %s", oldName, name);
	}


	/**
	 * Sets the classifications of a cause
	 * @param causeId the ID of the cause
	 * @param rawClassifications a string to be parsed in the form parent:child;parent:child;...
	 */
	public static void setClassifications(Long causeId, String rawClassifications) {
		Cause cause = Cause.findById(causeId);
		RCACase rcaCase = cause.rcaCase;

		// Authentication is done via function checkAuthentication(long causeId) automatically

		// Parse the raw string. The string should be a semicolon-delimited list of parent:child pairs delimited
		// by a colon. If an invalid item is found, an error page is returned and the method terminated before any
		// changes are done to the cause (ie. this method tries to be as atomic as possible).
		TreeSet<ClassificationPair> classificationPairs = new TreeSet<ClassificationPair>();
		if (rawClassifications.length() > 0) {
			String[] pairs = rawClassifications.split(";");
			String[] pair;
			Classification parent, child;
			for (String rawPair : pairs) {
				// Ensure that the length is correct
				pair = rawPair.split(":");
				if (pair.length != 2) {
					error();
					return;
				}

				// Ensure that the parent ID is valid
				parent = Classification.findById(Long.parseLong(pair[0]));
				if (parent == null) {
					error();
					return;
				}

				// Ensure that the child ID is valid
				child = Classification.findById(Long.parseLong(pair[1]));
				if (child == null) {
					error();
					return;
				}

				// Construct the pair and add to list
				ClassificationPair finalPair = ClassificationPair.createFromClassifications(parent, child);
				classificationPairs.add(finalPair);
			}
		}

		// If we get an empty rawClassifications, just remove all of the classifications
		else {
			classificationPairs = new TreeSet<ClassificationPair>();
		}

		cause.setClassifications(classificationPairs);
		cause.save();

		// Send event to stream
		CauseStream causeEvents = rcaCase.getCauseStream();
		CauseClassificationEvent event = new CauseClassificationEvent(causeId, classificationPairs);
		causeEvents.getStream().publish(event);
		Logger.info("Case %s reclassified with %d pair(s)", cause.name, classificationPairs.size());
	}

	private static boolean userAllowedToLike(User user, RCACase rcaCase, Cause cause) {
		return user != null && (rcaCase.getOwner().equals(user) || !cause.hasUserLiked(user));		
	}
	
	private static boolean userAllowedToDislike(User user, RCACase rcaCase, Cause cause) {
		return (cause.countLikes() > 0 && user != null &&
		        (rcaCase.getOwner().equals(user) || cause.hasUserLiked(user)));
	}
}
