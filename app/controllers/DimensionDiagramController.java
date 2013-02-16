/*
 * Copyright (C) 2012 by Eero Laukkanen, Risto Virtanen, Jussi Patana, Juha Viljanen,
 * Joona Koistinen, Pekka Rihtniemi, Mika Kek채le, Roope Hovi, Mikko Valjus,
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
import models.ClassificationRelationMap;
import models.RCACase;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@With(LanguageController.class)
public class DimensionDiagramController extends Controller {

	public static void nameEdge(Long fromId, Long toId, String name) {
		Classification from = Classification.findById(fromId);
		Classification to = Classification.findById(toId);

		RCACase rcaCase = from.rcaCase;
		User user = SecurityController.getCurrentUser();
		if (rcaCase.getOwner() == user) {
			ClassificationRelationMap relations = ClassificationRelationMap.fromCase(rcaCase);
			Map simpleRelations = relations.getSimpleRelations();
			Map relation = (Map)simpleRelations.get(from);
			((ClassificationRelationMap.ClassificationRelation)relation.get(to)).name = name;
			Logger.info(((ClassificationRelationMap.ClassificationRelation)relation.get(to)).name);
		}   else {
			Logger.info(":( "+ user +" "+ rcaCase.getOwner());
		}
	}

	public static void show(String URLHash) {
		RCACase rcaCase = RCACase.getRCACase(URLHash);
		notFoundIfNull(rcaCase);
		rcaCase = PublicRCACaseController.checkIfCurrentUserHasRightsForRCACase(rcaCase.id);
		notFoundIfNull(rcaCase);
		ClassificationRelationMap relations = ClassificationRelationMap.fromCase(rcaCase);
		HashMap<Long, Integer> classificationRelevance = new HashMap<Long, Integer>();
		if (relations == null) {
			relations = new ClassificationRelationMap();
		} else {
			classificationRelevance = relations.getClassificationRelevances();
		}
		if (classificationRelevance == null) {
			classificationRelevance = new HashMap<Long, Integer>();
		}
		render(rcaCase, relations, classificationRelevance);
	}
}
