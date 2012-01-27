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
import models.RCACase;
import models.User;
import models.events.*;
import play.Logger;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

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
		Cause cause = Cause.findById(Long.valueOf(causeId));
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
		Logger.info("Relation added between %s and %s", causeFrom, causeTo);
	}

	/**
	 * Checks whether a cause has corrective actions added.
	 *
	 * @param causeId
	 *
	 * @return
	 */
	public static boolean hasCorrections(Long causeId) {
		Cause cause = Cause.findById(causeId);
		return !cause.corrections.isEmpty();
	}

	/**
	 * Gets the name of the first corrective action of a cause.
	 *
	 * @param causeId
	 *
	 * @return
	 */
	public static String getFirstCorrectionName(Long causeId) {
		Cause cause = Cause.findById(causeId);
		return (cause.corrections).first().name;
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
		Logger.info("Correction added to cause %s with description %s", causeTo.name, description);
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
		Logger.info("Cause %s deleted", cause);
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

		NodeMovedEvent movedEvent = new NodeMovedEvent(cause, x, y);

		CauseStream causeStream = rcaCase.getCauseStream();
		causeStream.getStream().publish(movedEvent);
		Logger.info("Cause %s moved to x:%d, y:%d", cause, x, y);
	}
}
