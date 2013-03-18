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

import models.Classification;
import models.ClassificationDimension;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: jaffa
 * Date: 10/8/12
 * Time: 5:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassificationTest extends GenericRCAUnitTest {
	private String name;
	private String abbreviation;
	private String explanation;
	private ClassificationDimension classificationDimension;

	@Before
	public void setUp() {
		name = "Planning";
		abbreviation = "pla";
		explanation = "In planning phase.";
		classificationDimension = ClassificationDimension.valueOf(ClassificationDimension.WHERE_DIMENSION_ID);
	}

	@Test
	public void createClassificationTest() {
		super.setUp();
		Classification classification = new Classification(testCase, name, user,
		                                                   ClassificationDimension.WHERE_DIMENSION_ID,
		                                                   explanation, abbreviation);
		assertTrue(classification.name.equals(name));
		assertTrue(classification.creatorId.equals(user.getId()));
		assertTrue(classification.explanation.equals(explanation));
		assertTrue(classification.abbreviation.equals(abbreviation));
	}
}
