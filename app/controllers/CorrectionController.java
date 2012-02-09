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
import play.Logger;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;

/**
 * Controller for corrective actions.
 * @author Eero Laukkanen
 */
public class CorrectionController extends Controller {

	/**
	 * Check that user has rights to edit the correction.
	 *
	 * @param correctionId correctionId that will be edited
	 */
	@Before
	public static void checkAuthentication(Long correctionId) {
		validation.required(correctionId);
		if (Validation.hasErrors()) {
			forbidden();
		}
		Correction correction = Correction.findById(correctionId);
		Cause cause = correction.cause;
		PublicRCACaseController.checkIfCurrentUserHasRightsForRCACase(cause.rcaCase.id);
	}

	/**
	 * Adds a like to the correction if user has rights to do it.
	 * @param correctionId id of the correction to be liked
	 */
	public static void like(Long correctionId) {
		Correction correction = Correction.findById(correctionId);
		RCACase rcaCase = correction.cause.rcaCase;
		User user = SecurityController.getCurrentUser();
		if (user == null) {
			forbidden();
		}
		if (rcaCase.getOwner().equals(user)) {
			correction.like(user);
		} else if (!correction.hasUserLiked(user)) {
			correction.like(user);
		}
		Logger.debug("Correction %s liked by %s", correction, user);

		String likeData = String.format("{\"count\":%d,\"hasliked\":%b,\"isowner\":%b}", correction.countLikes(),
		                                correction.hasUserLiked(user), user.equals(rcaCase.getOwner()));

		renderJSON(likeData);
	}

	/**
	 * Removes a like from the correction.
	 * @param correctionId id of the correction to be disliked
	 */
	public static void dislike(Long correctionId) {
		Correction correction = Correction.findById(correctionId);
		RCACase rcaCase = correction.cause.rcaCase;
		User user = SecurityController.getCurrentUser();
		if (user == null) {
			forbidden();
		}
		correction.dislike(user);
		Logger.debug("Correction %s disliked by %s", correction, user);
		String likeData = String.format("{\"count\":%d,\"hasliked\":%b,\"isowner\":%b}", correction.countLikes(),
		                                correction.hasUserLiked(user), user.equals(rcaCase.getOwner()));

		renderJSON(likeData);
	}
}
