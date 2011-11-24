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

import job.Bootstrap;
import models.*;
import models.enums.CompanySize;
import models.enums.RCACaseType;
import org.junit.*;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Set;

/**
 * @author Eero Laukkanen
 */
public class CauseTest extends UnitTest {

	private User user;
	private RCACaseType rcaCaseType;
	private CompanySize size;
	private RCACase testCase;

	@Before
	public void setUp() {
		Fixtures.deleteAllModels();
		new Bootstrap().doJob();
		user = User.find("byEmail", "admin@local").first();
		rcaCaseType = RCACaseType.valueOf(2);
		size = CompanySize.valueOf(2);
		RCACase rcaCase = new RCACase("TestRCACase", rcaCaseType.value, "Kaapelissa ei vikaa", "Kaapelissa vikaa",
		                              true, "Keijon Kaapeli ja Kaivanto Oy", size.value, "Kaapelit ja johtimet",
		                              false, user);
		testCase = user.addRCACase(rcaCase);
	}

	@Test
	public void addCauseTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user);
		Cause cause2 = new Cause(testCase, "test cause2", user);
		cause1.addCause(cause2);
		assertTrue(cause2.isChildOf(cause1));
		Cause cause3 = cause1.addCause("test cause3", user);
		assertTrue(cause3.isChildOf(cause1));
	}

	@Test
	public void isParentTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user);
		Cause cause2 = new Cause(testCase, "test cause2", user);
		Cause cause3 = new Cause(testCase, "test cause3", user);
		cause1.addCause(cause2);
		assertTrue(cause2.isChildOf(cause1));
		cause3.addCause(cause2);
		assertFalse(cause2.isChildOf(cause3));
	}

	@Test
	public void getCausesTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user);
		Cause cause2 = new Cause(testCase, "test cause2", user);
		Cause cause3 = new Cause(testCase, "test cause3", user);
		Cause cause4 = new Cause(testCase, "test cause4", user);
		cause1.addCause(cause2);
		cause1.addCause(cause3);
		Set<Cause> causes = cause1.causes;
		assertTrue(causes.contains(cause2));
		assertTrue(causes.contains(cause3));
		assertFalse(causes.contains(cause4));
	}

	@Test
	public void deleteTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user);
		Cause cause2 = new Cause(testCase, "test cause2", user);
		cause1.addCause(cause2);
		cause2.deleteCause();
		assertFalse(cause1.causes.contains(cause2));
	}

	@Test
	public void correctionTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user);
		Correction correction = cause1.addCorrection("new correction", "correction for a cause");
		assertTrue(correction.cause.equals(cause1));
		assertTrue(cause1.corrections.contains(correction));
		Correction correction2 = Correction.find("byName", "new correction").first();
		assertNotNull(correction2);
		cause1.removeCorrection(correction);
		Correction correction3 = Correction.find("byName", "new correction").first();
		assertNull(correction3);
	}
}
