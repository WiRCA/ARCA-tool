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
import models.enums.StatusOfCause;
import models.enums.StatusOfCorrection;
import play.data.binding.As;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;

import javax.persistence.Query;
import java.util.*;

/**
 * @author Risto Virtanen
 */
@With({LanguageController.class})
public class MonitoringController extends Controller {

	public static void index() {
		StatusOfCause[] causeStatuses = StatusOfCause.values();
		StatusOfCorrection[] correctionStatuses = StatusOfCorrection.values();
		render(causeStatuses, correctionStatuses);
	}

	public static void rcaCaseSelecting(@As(",") List<String> showCases) {
		if (showCases.contains("allCases")) {
			render();
		}
		Set<RCACase> cases = new HashSet<RCACase>();
		User user = SecurityController.getCurrentUser();
		if (showCases.contains("publicCases")) {
			List<RCACase> publicCases = RCACase.find("isCasePublic", true).fetch();
			cases.addAll(publicCases);
		}
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

	public static void causesAndCorrections(@As(",") List<String> whatToShow, @As(",") List<Long> selectedCases,
	                                        Boolean allCases, @As(",") List<Integer> selectedCauseStatuses,
	                                        @As(",") List<Integer> selectedCorrectionStatuses) {
		Boolean showCauses = whatToShow.contains("causes");
		Boolean showCorrections = whatToShow.contains("corrections");
		User user = SecurityController.getCurrentUser();
		Set<RCACase> rcaCases = new HashSet<RCACase>();
		if (allCases) {
			if (user != null) {
				rcaCases.addAll(user.getRCACases());
			}
			List<RCACase> publicCases = RCACase.find("isCasePublic", true).fetch();
			rcaCases.addAll(publicCases);
		} else {
			List<RCACase> selected = JPA.em().createQuery("SELECT r FROM rcacase r WHERE r.id in ?1")
										.setParameter(1, selectedCases).getResultList();
			rcaCases.addAll(selected);
		}
		
		if (selectedCases.isEmpty()) {
			render(showCorrections);
		}

		Long currentUserId = user != null ? user.id : -1;

		List<Cause> causes = null;
		List<Correction> corrections = null;
		if (showCauses) {
			Query query;
			if (selectedCauseStatuses.size() >= 1 && selectedCauseStatuses.get(0) != null) {
				query = JPA.em().createQuery("SELECT c FROM cause c WHERE c.rcaCase in ?1 and c.statusValue in ?2")
				           .setParameter(1, rcaCases).setParameter(2, selectedCauseStatuses);
			} else {
				query = JPA.em().createQuery("SELECT c FROM cause c WHERE c.rcaCase in ?1")
				           .setParameter(1, rcaCases);
			}
			causes = query.getResultList();
		} else if (showCorrections) {
			Query query;
			if (selectedCorrectionStatuses.size() >= 1 && selectedCorrectionStatuses.get(0) != null) {
				query = JPA.em().createQuery("SELECT c FROM correction c WHERE c.cause.rcaCase in ?1 and c.statusValue in ?2")
				           .setParameter(1, rcaCases).setParameter(2, selectedCorrectionStatuses);
			} else {
				query = JPA.em().createQuery("SELECT c FROM correction c WHERE c.cause.rcaCase in ?1")
				           .setParameter(1, rcaCases);
			}
			corrections = query.getResultList();
		}

		StatusOfCause[] causeStatuses = StatusOfCause.values();
		StatusOfCorrection[] correctionStatuses = StatusOfCorrection.values();

		render(user, currentUserId, showCauses, causes,showCorrections,
		       corrections, causeStatuses, correctionStatuses);
	}

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
