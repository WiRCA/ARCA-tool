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
import models.enums.StatusOfCause;
import models.enums.StatusOfCorrection;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.With;

import java.util.*;

/**
 * @author Risto Virtanen
 */
@With({LanguageController.class})
public class MonitoringController extends Controller {

	/**
	* Opens the index page of the Monitoring
	*/
	public static void index() {
		StatusOfCause[] causeStatuses = StatusOfCause.values();
		StatusOfCorrection[] correctionStatuses = StatusOfCorrection.values();
		render(causeStatuses, correctionStatuses);
	}

	/**
	* List the rca cases that the user wants to see
	* @param showCases e.g. "publicCases", "sharedCases", or "myCases"
	*/
	public static void rcaCaseSelecting(@As(",") List<String> showCases) {
		Set<RCACase> cases = new HashSet<RCACase>();
		User user = SecurityController.getCurrentUser();
		if (showCases.contains("sharedCases")) {
			if (user != null) {
				List<RCACase> sharedCases = new ArrayList<RCACase>();
				for (RCACase rcaCase : user.getRCACases()) {
					if (!rcaCase.ownerId.equals(user.id)) {
						sharedCases.add(rcaCase);
					}
				}
				cases.addAll(sharedCases);
			}
		}
		if (showCases.contains("myCases")) {
			if (user != null) {
				List<RCACase> myCases = RCACase.find("ownerId", user.id).fetch();
				cases.addAll(myCases);
			}
		}
		render(cases);
	}

	public static void dimensionDiagram(@As(",") List<Long> selectedCases) {
		ClassificationRelationMap map = new ClassificationRelationMap();
		for (Long id : selectedCases) {
			map.loadCase((RCACase) RCACase.findById(id));
		}
		renderJSON(map.toJson());
	}

	public static void classificationTable(@As(",") List<Long> selectedCases) {
		ClassificationTable table = new ClassificationTable();
		for (Long id : selectedCases) {
			table.loadCase((RCACase) RCACase.findById(id));
		}
		renderJSON(table.toJson());
	}

	/**
	* Lists the causes and corrective actions that user wants to see
	* @param whatToShow "causes", "corrections", or both
	* @param selectedCases ids of the rca cases to be shown
	* @param allCases if user wants to see all cases that he has rights for
	* @param selectedCauseStatuses statuses of the causes that the user wants to see
	* @param selectedCorrectionStatuses statuses of the corrective actions that the user wants to see
	* @param csvExport if the request is to download csv file
	*/
	public static void causesAndCorrections(@As(",") List<String> whatToShow, @As(",") List<Long> selectedCases,
	                                        Boolean allCases, @As(",") List<Integer> selectedCauseStatuses,
	                                        @As(",") List<Integer> selectedCorrectionStatuses, Boolean csvExport) {
		Boolean showCauses = whatToShow.contains("causes");
		Boolean showCorrections = whatToShow.contains("corrections");
		User user = SecurityController.getCurrentUser();
		if (allCases) {
			selectedCases = new ArrayList<Long>();
			if (user != null) {
				for (RCACase rcaCase : user.getRCACases()) {
					selectedCases.add(rcaCase.id);
				}
			}
			List<RCACase> publicCases = RCACase.find("isCasePublic", true).fetch();
			for (RCACase rcaCase : publicCases) {
				selectedCases.add(rcaCase.id);
			}
		}

		Long currentUserId = user != null ? user.id : -1;

		List<Cause> causes = null;
		List<Correction> corrections = null;
		if (showCauses) {
			if (selectedCauseStatuses.get(0) != null) {
				causes = Cause.find("rcaCase.id in (?1) and statusValue in (?2)", selectedCases,
				                    selectedCauseStatuses).fetch();
			} else {
				causes = Cause.find("rcaCase.id in (?1)", selectedCases).fetch();
			}
		} else if (showCorrections) {
			if (selectedCorrectionStatuses.get(0) != null) {
				corrections = Correction.find("cause.rcaCase.id in (?1) AND statusValue in (?2)", selectedCases,
				                              selectedCorrectionStatuses).fetch();
			} else {
				corrections = Correction.find("cause.rcaCase.id in (?1)", selectedCases).fetch();
			}
		}

		if (csvExport) {
			response.setHeader("Content-Disposition", "attachment;filename=arca-monitoring.csv");
			request.format = "text/csv";
			renderTemplate("MonitoringController/extractCSV.csv", user, currentUserId, showCauses, causes,
			               showCorrections, corrections);
		}

		StatusOfCause[] causeStatuses = StatusOfCause.values();
		StatusOfCorrection[] correctionStatuses = StatusOfCorrection.values();

		render(user, currentUserId, showCauses, causes, showCorrections,
		       corrections, causeStatuses, correctionStatuses);
	}

	/**
	* Change the status of a cause
	* @param causeId the id of the cause to be updated
	* @param statusOfCause the new status of the cause
	*/
	public static void changeCauseStatus(Long causeId, StatusOfCause statusOfCause) {
		Cause cause = Cause.findById(causeId);
		notFoundIfNull(cause);
		User currentUser = SecurityController.getCurrentUser();
		if (currentUser != null && currentUser.id.equals(cause.rcaCase.ownerId)) {
			cause.setStatus(statusOfCause);
			cause.save();
			renderJSON(true);
		}
		renderJSON(false);
	}

	/**
	* Change the status of a corrective action
	* @param correctionId the id of the corrective action to be updated
	* @param statusOfCorrection the new status of the corrective action
	*/
	public static void changeCorrectionStatus(Long correctionId, StatusOfCorrection statusOfCorrection) {
		Correction correction = Correction.findById(correctionId);
		notFoundIfNull(correction);
		User currentUser = SecurityController.getCurrentUser();
		if (currentUser != null && currentUser.id.equals(correction.cause.rcaCase.ownerId)) {
			correction.setStatus(statusOfCorrection);
			correction.save();
			renderJSON(true);
		}
		renderJSON(false);
	}
}
