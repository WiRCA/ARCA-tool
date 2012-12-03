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

import models.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class ClassificationRelationMapTest extends GenericRCAUnitTest {
	List<Classification> whatClassifications;
	List<Classification> whereClassifications;
	List<Cause> causes;

	@Before
	public void setUp() {
		super.setUp();
		this.causes = new ArrayList<Cause>();
		Cause cause;
		for (int i = 0; i < 5; i++) {
			cause = new Cause(testCase, "Cause " + String.valueOf(i), user);
			testCase.setProblem(cause);
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
			whereClassifications.add(new Classification(testCase, "Where " + String.valueOf(i), user,
			                                            ClassificationDimension.WHERE_DIMENSION_ID, "", ""));
		}
	}

	@Test
	public void classificationSimpleRelationTest() {
		Classification classification1 = whereClassifications.get(0);
		Classification classification2 = whereClassifications.get(1);

		SortedSet<ClassificationPair> pairs = new TreeSet<ClassificationPair>();
		pairs.add(new ClassificationPair(classification1, whatClassifications.get(0)));
		causes.get(0).setClassifications(pairs);

		pairs = new TreeSet<ClassificationPair>();
		pairs.add(new ClassificationPair(classification2, whatClassifications.get(0)));
		causes.get(1).setClassifications(pairs);

		ClassificationRelationMap map = ClassificationRelationMap.fromCase(testCase);
		HashMap<Classification, HashMap<Classification, ClassificationRelationMap.ClassificationRelation>> relations =
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
}
