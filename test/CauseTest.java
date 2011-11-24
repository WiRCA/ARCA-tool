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
import org.codehaus.groovy.tools.shell.commands.ExitCommand;
import org.junit.*;
import play.Logger;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.Set;
import java.util.Date;

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
	public void getCreatorAndParentTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user);
		assertTrue(cause1.getCreator().equals(user));
		assertNull(cause1.parent);
	}

	@Test
	public void updateCauseTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user);
		Date date1 = cause1.rcaCase.updated;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Logger.fatal("Sleep failed in a test");
			assert true;
		}
		cause1.name = "test cause1 modified";
		cause1.save();
		assertTrue(cause1.rcaCase.updated.after(date1));
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
		assertTrue(cause2.parent.equals(cause1));
		cause3.addCause(cause2);
		assertFalse(cause2.isChildOf(cause3));
	}

	@Test
	public void getCausesAndRelationsTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user);
		Cause cause2 = new Cause(testCase, "test cause2", user);
		Cause cause3 = new Cause(testCase, "test cause3", user);
		Cause cause4 = new Cause(testCase, "test cause4", user);
		cause1.addCause(cause2);
		cause1.addCause(cause3);
		cause2.addCause(cause4);
		cause1.addCause(cause4);
		Set<Cause> causes = cause1.causes;
		Set<Cause> relations = cause1.relations;
		assertTrue(causes.contains(cause2));
		assertTrue(causes.contains(cause3));
		assertFalse(causes.contains(cause4));
		assertTrue(relations.contains(cause4));
		assertFalse(relations.contains(cause2));
	}

	@Test
	public void deleteTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user);
		Cause cause2 = new Cause(testCase, "test cause2", user);
		Cause cause3 = new Cause(testCase, "test cause3", user);
		cause1.addCause(cause2);
		cause2.addCause(cause3);
		cause2.deleteCause();
		assertFalse(cause1.causes.contains(cause2));
		assertTrue(cause3.effectRelations.isEmpty());
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
