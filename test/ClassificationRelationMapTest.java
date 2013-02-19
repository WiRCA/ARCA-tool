/*
 * Copyright (C) 2011 - 2013 by Eero Laukkanen, Risto Virtanen, Jussi Patana,
 * Juha Viljanen, Joona Koistinen, Pekka Rihtniemi, Mika Kekäle, Roope Hovi,
 * Mikko Valjus, Timo Lehtinen, Jaakko Harjuhahto, Jonne Viitanen, Jari Jaanto,
 * Toni Sevenius, Anssi Matti Helin, Jerome Saarinen, Markus Kere
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

import models.*;
import org.junit.Before;
import org.junit.Test;
import play.Logger;

import java.util.*;

public class ClassificationRelationMapTest extends GenericRCAUnitTest {
	List<Classification> whatClassifications;
	List<Classification> whereClassifications;
	List<Cause> causes;

	@Before
	public void setUp() {
		super.setUp();

		this.causes = new ArrayList<Cause>();
		Cause cause = null;
		for (int i = 0; i < 5; i++) {
			cause = new Cause(testCase, "Cause " + String.valueOf(i), user);
			cause.save();
			this.causes.add(cause);
		}

		Cause fromCause = this.causes.get(0);
		Cause toCause = this.causes.get(1);
		fromCause.addCause(toCause);

		this.whatClassifications = new ArrayList<Classification>();
		this.whereClassifications = new ArrayList<Classification>();

		for (int i = 0; i < 10; i++) {
			whatClassifications.add(new Classification(testCase, "What " + String.valueOf(i), user,
			                                           ClassificationDimension.WHAT_DIMENSION_ID, "", ""));
			whatClassifications.get(i).save();
			whereClassifications.add(new Classification(testCase, "Where " + String.valueOf(i), user,
			                                            ClassificationDimension.WHERE_DIMENSION_ID, "", ""));
			whereClassifications.get(i).save();
		}
	}

	@Test
	public void classificationSimpleRelationTest() {
		Classification classification1 = whereClassifications.get(0);
		Classification classification2 = whereClassifications.get(1);

		SortedSet<ClassificationPair> pairs = new TreeSet<ClassificationPair>();
		pairs.add(new ClassificationPair(classification1, whatClassifications.get(0)));
		testCase.causes.add(causes.get(0));
		causes.get(0).setClassifications(pairs);

		pairs = new TreeSet<ClassificationPair>();
		pairs.add(new ClassificationPair(classification2, whatClassifications.get(0)));
		testCase.causes.add(causes.get(1));
		causes.get(1).setClassifications(pairs);

		ClassificationRelationMap map = ClassificationRelationMap.fromCase(testCase);
		Map<Classification, Map<Classification, ClassificationRelationMap.ClassificationRelation>> relations =
				map.getSimpleRelations();

		assertTrue(relations.containsKey(classification2));
		assertTrue(relations.get(classification2).containsKey(classification1));
		assertEquals(1, relations.get(classification2).get(classification1).strength);
	}


	@Test
	public void classificationPairRelationTest() {
		Classification where1 = whereClassifications.get(0);
		Classification where2 = whereClassifications.get(1);
		Classification what1 = whatClassifications.get(0);
		Classification what2 = whatClassifications.get(1);

		SortedSet<ClassificationPair> pairs = new TreeSet<ClassificationPair>();
		pairs.add(new ClassificationPair(where1, what1));
		causes.get(0).setClassifications(pairs);

		pairs = new TreeSet<ClassificationPair>();
		pairs.add(new ClassificationPair(where2, what2));
		causes.get(1).setClassifications(pairs);
	}


	@Test
	public void multipleCaseTest() {
		ClassificationRelationMap map = new ClassificationRelationMap();

		// First case
		Cause cause1 = this.causes.get(0);
		Cause cause2 = this.causes.get(1);
		Cause cause3 = this.causes.get(2);

		testCase.causes.add(cause1);
		testCase.causes.add(cause2);
		testCase.causes.add(cause3);

		testCase.problem.addCause(cause1);
		cause1.addCause(cause2);
		testCase.problem.addCause(cause3);
		cause2.addCause(cause3);
		cause3.addCause(cause1);
		cause1.classifications.add(new ClassificationPair(whereClassifications.get(0), whatClassifications.get(0)));
		cause2.classifications.add(new ClassificationPair(whereClassifications.get(1), whatClassifications.get(1)));
		cause3.classifications.add(new ClassificationPair(whereClassifications.get(2), whatClassifications.get(2)));
		map.loadCase(testCase);

		// Second case
		cause1.classifications = new TreeSet<ClassificationPair>();
		cause2.classifications = new TreeSet<ClassificationPair>();
		cause3.classifications = new TreeSet<ClassificationPair>();
		cause1.classifications.add(new ClassificationPair(whereClassifications.get(2), whatClassifications.get(2)));
		cause2.classifications.add(new ClassificationPair(whereClassifications.get(3), whatClassifications.get(3)));
		cause3.classifications.add(new ClassificationPair(whereClassifications.get(4), whatClassifications.get(4)));
		map.loadCase(testCase);

		// Assertions
		assertNotNull(map);
		Map<Classification, Map<Classification, ClassificationRelationMap.ClassificationRelation>> relations =
				map.getSimpleRelations();

		// Each simple relation key exists
		for (int i = 0; i < 5; i++) {
			assertTrue(relations.containsKey(whereClassifications.get(i)));
		}

		// The correct relations
		assertTrue(relations.get(whereClassifications.get(3)).containsKey(whereClassifications.get(2)));
		assertTrue(relations.get(whereClassifications.get(4)).containsKey(whereClassifications.get(3)));
		assertTrue(relations.get(whereClassifications.get(1)).containsKey(whereClassifications.get(0)));
		assertTrue(relations.get(whereClassifications.get(2)).containsKey(whereClassifications.get(4)));
		assertTrue(relations.get(whereClassifications.get(2)).containsKey(whereClassifications.get(1)));
		assertTrue(relations.get(whereClassifications.get(0)).containsKey(whereClassifications.get(2)));
	}
}