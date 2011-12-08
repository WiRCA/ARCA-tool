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

import models.Correction;
import models.RCACase;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;
import models.Cause;
import models.events.*;
import models.events.AddCauseEvent;
import models.events.DeleteCauseEvent;

import java.util.SortedSet;

/**
 * @author Eero Laukkanen
 * @author Juha Viljanen
 */

@With({LanguageController.class})
public class CauseController extends Controller {

	public static void addCause(String causeId, String name) {
		// causeId is used later as a String
		Cause cause = Cause.findById(Long.valueOf(causeId));
		RCACase rcaCase = cause.rcaCase;

		Cause newCause = cause.addCause(name, SecurityController.getCurrentUser());

		AddCauseEvent event = new AddCauseEvent(newCause, causeId);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.info("Cause %s added to cause %s", name, cause);
	}

	public static void addRelation(Long fromId, Long toID) {
		Cause causeFrom = Cause.findById(fromId);
		RCACase rcaCase = causeFrom.rcaCase;
		
		Cause causeTo = Cause.findById(toID);
		
		causeFrom.addCause(causeTo);
		causeFrom.save();
		causeTo.save();
    
		AddRelationEvent event = new AddRelationEvent(Long.toString(fromId), Long.toString(toID));
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.info("Relation added between %s and %s", causeFrom, causeTo);
	}
	
	public static boolean hasCorrections(Long causeId) {
	  Cause cause = Cause.findById(causeId);
	  Logger.info("%s", cause.corrections.size());
	  return !cause.corrections.isEmpty();
	}
	
	public static String getFirstCorrectionName(Long causeId) {
	  Cause cause = Cause.findById(causeId);
	  return ((SortedSet<Correction>)cause.getCorrections()).first().name;
  }
	
	public static void addCorrection(Long toId, String name) {
	  Cause causeTo = Cause.findById(toId);
	  RCACase rcaCase = causeTo.rcaCase;
	  
	  causeTo.addCorrection(name, " ");
	  causeTo.save();
	  
	  AddCorrectionEvent event = new AddCorrectionEvent(causeTo, name);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.info("Correction added to cause %s", causeTo.name);
	}

	public static void deleteCause(String causeId) {
		Cause cause = Cause.findById(Long.valueOf(causeId));
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
}
