import models.ProblemCause;
import models.ProblemDefinition;
import models.RCACase;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import javax.mail.Session;


/*
 * Copyright (C) 2011 by Eero Laukkanen, Risto Virtanen, Jussi Patana, Juha Viljanen, Joona Koistinen, Pekka Rihtniemi, Mika Kekäle, Roope Hovi, Mikko Valjus
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


/*
	* Create new problem causes for our RCA cases problem definition(s) that were selected in the previous step
	* . Create
	* more causes related to previously created causes, and then remove different causes. Select some of the root
	* causes for the next step of the RCA case.
	*
	* ProblemCause
	*   - title of problem cause
	*   - problem definition
	*   - corrective actions
	*   - causes ManyToMany
	*
	 */

public class ProblemCauseTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		RCACase rcaCase = new RCACase("Test RCA case.");
		rcaCase.save();
		ProblemDefinition probDef = new ProblemDefinition("Out of coffee", rcaCase.id);
		probDef.save();
		ProblemCause probCause = new ProblemCause("Out of coffee grounds");
		probCause.save();
		probDef.addCause(probCause);
		ProblemCause probCause2 = new ProblemCause("Out of money");
		probCause2.save();
		probCause.addCause(probCause2);
		probCause2.refresh();
		probCause2.save();
	}

	@Test
	public void problemCauseNotNull() {
		ProblemCause probCause = ProblemCause.find("byName", "Out of coffee grounds").first();
		assertNotNull(probCause);
	}

	@Test
	public void problemCauseHasDefinition() {
		ProblemCause probCause = ProblemCause.find("byName", "Out of coffee grounds").first();
		ProblemDefinition probDef = ProblemDefinition.find("byName", "Out of coffee").first();
		assertTrue(probCause.isCauseOf(probDef));
	}

	@Test
	public void CauseHasCause() {
		ProblemCause probCause = ProblemCause.find("byName", "Out of coffee grounds").first();
		ProblemCause probCause2 = ProblemCause.find("byName", "Out of money").first();
		probCause.addCause(probCause2);
	}





}
