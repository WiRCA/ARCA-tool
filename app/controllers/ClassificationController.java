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

import models.Classification;
import models.RCACase;
import models.User;
import models.ClassificationDimension;
import models.events.AddClassificationEvent;
import models.events.CauseStream;
import models.events.EditClassificationEvent;
import models.events.RemoveClassificationEvent;
import org.hibernate.exception.ConstraintViolationException;
import play.Logger;
import play.exceptions.JavaExecutionException;
import play.mvc.Controller;
import play.mvc.With;

import javax.persistence.PersistenceException;


/**
 * Methods related to classification manipulation.
 * @author AM Helin
 * @author Toni Sevenius
 */
@With({LanguageController.class})
public class ClassificationController extends Controller {

	/**
	 * Creates a classification for the given case.
	 * @param rcaCaseId the ID of the case the classification belongs to
	 * @param type the type of the classification
	 * @param name the name of the classification
	 * @param abbreviation the abbreviation of the classification
	 * @param explanation the long explanation of the classification
	 */
	public static void createClassification(Long rcaCaseId,
			                                int type,
			                                String name,
			                                String abbreviation,
			                                String explanation) {

		// Ensure that the case exists
		RCACase rcaCase = RCACase.findById(rcaCaseId);
		if (rcaCase == null) {
			renderJSON("{\"error\": \"Invalid RCA case\"}");
			return;
		}

		// Ensure that the user is allowed to create classifications for the case
		if (!ClassificationController.canEditClassifications(rcaCase)) {
			renderJSON("{\"error\": \"Permission denied\"}");
			return;
		}

		// Ensure that the classification type is correct
		if (ClassificationDimension.valueOf(type) == null) {
			renderJSON("{\"error\": \"Invalid classification type\"}");
		}

		Classification classification = new Classification(rcaCase, name, SecurityController.getCurrentUser(),
		                                                   type, explanation, abbreviation);
		classification.save();

		AddClassificationEvent event = new AddClassificationEvent(classification.id, name, type,
		                                                          abbreviation, explanation);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.debug("Classification %s (%s) added for case %s", name, ClassificationDimension.valueOf(type),
		                                                         rcaCase.caseName);
		renderJSON("{\"success\": true}");
	}


	/**
	 * Edits the given classification.
	 * @param rcaCaseId the ID of the case the classification belongs to
	 * @param classificationId the ID of the classification
	 * @param type the new type of the classification
	 * @param name the new name of the classification
	 * @param abbreviation the new abbreviation of the classification
	 * @param explanation the new long explanation of the classification
	 */
	public static void editClassification(Long rcaCaseId,
	                                      Long classificationId,
	                                      int type,
	                                      String name,
	                                      String abbreviation,
	                                      String explanation) {

		// Ensure that the case exists
		if (rcaCaseId == null) {
			renderJSON("{\"error\": \"Invalid RCA case\"}");
			return;
		}
		RCACase rcaCase = RCACase.findById(rcaCaseId);
		if (rcaCase == null) {
			renderJSON("{\"error\": \"Invalid RCA case\"}");
			return;
		}

		// Ensure that the user is allowed to create classifications for the case
		if (!ClassificationController.canEditClassifications(rcaCase)) {
			renderJSON("{\"error\": \"Permission denied\"}");
			return;
		}

		// Ensure that the classification type is correct
		if (ClassificationDimension.valueOf(type) == null) {
			renderJSON("{\"error\": \"Invalid classification type\"}");
			return;
		}

		Classification classification = Classification.findById(classificationId);
		if (classification == null) {
			renderJSON("\"error\": \"Invalid cause ID\"");
			return;
		}

		classification.abbreviation = abbreviation;
		classification.explanation = explanation;
		classification.name = name;
		classification.classificationDimension = type;
		classification.save();

		EditClassificationEvent event = new EditClassificationEvent(classification.id, name, type,
		                                                            abbreviation, explanation);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.debug("Classification %s (%s) edited for case %s", name, ClassificationDimension.valueOf(type),
		             rcaCase.caseName);
		renderJSON("{\"success\": true}");
	}


	/**
	 * Edits the given classification.
	 * @param rcaCaseId the ID of the case the classification belongs to
	 * @param classificationId the ID of the classification
	 */
	public static void removeClassification(Long rcaCaseId,
	                                        Long classificationId) {

		// Ensure that the case exists
		RCACase rcaCase = RCACase.findById(rcaCaseId);
		if (rcaCase == null) {
			renderJSON("{\"error\": \"Invalid RCA case\"}");
			return;
		}

		// Ensure that the user is allowed to create classifications for the case
		if (!ClassificationController.canEditClassifications(rcaCase)) {
			renderJSON("{\"error\": \"Permission denied\"}");
			return;
		}

		Classification classification = Classification.findById(classificationId);
		if (classification == null) {
			renderJSON("{\"error\": \"Invalid cause ID\"}");
			return;
		}

		try {
			classification.delete();
		} catch (PersistenceException e) {
			renderJSON("{\"error\": \"The classification is in use.\"}");
			return;
		}

		RemoveClassificationEvent event = new RemoveClassificationEvent(classification.id,
		                                                                classification.classificationDimension);
		CauseStream causeEvents = rcaCase.getCauseStream();
		causeEvents.getStream().publish(event);
		Logger.debug("Classification #%d removed from case %s", classificationId, rcaCase.caseName);
		renderJSON("{\"success\": true}");
	}


	/**
	 * Returns whether or not the current user is allowed to create and edit classifications for the given case.
	 * @param rcaCase the RCA case to check against
	 * @return whether the current user is allowed to create a classification
	 */
	public static boolean canEditClassifications(RCACase rcaCase) {
		User current = SecurityController.getCurrentUser();
		return current != null && current.id.equals(rcaCase.ownerId);
	}

}
