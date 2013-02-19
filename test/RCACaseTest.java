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

import models.Cause;
import models.RCACase;
import models.User;
import models.enums.CompanySize;
import models.enums.RCACaseType;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

/**
 * @author Mikko Valjus
 */
public class RCACaseTest extends UnitTest {
	private User user;
	private RCACaseType rcaCaseType;
	private CompanySize size;

	@Before
	public void setUp() {
		user = User.find("byEmail", "admin@local").first();
		rcaCaseType = RCACaseType.valueOf(2);
		size = CompanySize.valueOf(2);
	}

	@Test
	public void createRCACaseTest() {
		RCACase testCase = new RCACase("TestRCACase", rcaCaseType.value, "Kaapelissa ei vikaa", "Kaapelissa vikaa",
		                               true, "Keijon Kaapeli ja Kaivanto Oy", size.value, "Kaapelit ja johtimet",
		                               false, user);
		user.addRCACase(testCase);
		assertTrue(user.caseIds.contains(testCase.id));
		RCACase comparisonCase = RCACase.find("byID", testCase.id).first();
		assertEquals(comparisonCase.companyName, "Keijon Kaapeli ja Kaivanto Oy");
		assertEquals(testCase.getRCACaseType(), RCACaseType.valueOf(2));
		assertNotSame(testCase.getCompanySize(), CompanySize.valueOf(3));
		assertEquals(testCase.ownerId, user.id);
		assertEquals(testCase.caseGoals, "Kaapelissa ei vikaa");
		assertEquals(testCase.description, "Kaapelissa vikaa");
		assertEquals(testCase.companyProducts, "Kaapelit ja johtimet");
		assertFalse(testCase.isCasePublic);
		assertTrue(testCase.isMultinational);
		assertNotNull(testCase.created);
		assertEquals(comparisonCase.caseName, "TestRCACase");
	}

	@Test
	public void getAndSetTest() {
		RCACase testCase = new RCACase(user);
		user.addRCACase(testCase, "another name");		
		assertTrue(testCase.getOwner().equals(user));
		assertTrue(testCase.problem.name.equals("another name"));
		testCase.setRCACaseType(rcaCaseType);
		assertTrue(testCase.getRCACaseType() == rcaCaseType);
		testCase.setCompanySize(size);
		assertTrue(testCase.getCompanySize() == size);
		assertFalse(testCase.toString().equals(""));
	}

	@Test
	public void deleteCauseTest() {
		RCACase testCase = new RCACase(user).save();
		testCase.problem = new Cause(testCase, "really unique test cause", user).save();
		Cause cause1 = testCase.problem;
		Cause cause2 = cause1.addCause("another unique test cause", user).save();
		testCase.save();
		assertTrue(testCase.causes.contains(cause1));
		long count = Cause.count();
		testCase.deleteCause(cause2);
		assertFalse(testCase.causes.contains(cause2));
		assertFalse(count == Cause.count());
		Cause isFound = Cause.find("byName", "another unique test cause").first();
		assertNull(isFound);
		testCase.deleteCause(cause1);
		assertTrue(testCase.causes.contains(cause1)); // cause1 is the problem, therefore not deleted
	}

}
