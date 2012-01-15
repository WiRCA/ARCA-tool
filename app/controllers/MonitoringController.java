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
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Risto Virtanen
 */
@With({LanguageController.class})
public class MonitoringController extends Controller {

	public static void index() {
		render();
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

	public static void causesAndCorrections(@As(",") List<String> whatToShow, String selectedCases, Boolean allCases) {
		Boolean showCorrections = whatToShow.contains("corrections");
		if (allCases) {
			selectedCases = "";
			User user = SecurityController.getCurrentUser();
			if (user != null) {
				for (RCACase rcaCase : user.getRCACases()) {
					selectedCases += rcaCase.id + ",";
				}
			}
			List<RCACase> publicCases = RCACase.find("isCasePublic", true).fetch();
			for (RCACase rcaCase : publicCases) {
				selectedCases += rcaCase.id + ",";
			}
		}

		if (selectedCases.isEmpty()) {
			render(showCorrections);
		}
		
		selectedCases = "(" + selectedCases.substring(0, selectedCases.lastIndexOf(",")) + ")";
		String query = "select c from cause c where rcaCaseId in (" + selectedCases + ")";
		List<Cause> causes = Cause.find("rcaCaseId in " + selectedCases).fetch();
		render(causes, showCorrections);
	}
}
