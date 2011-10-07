/**
 * Created by IntelliJ IDEA.
 * User: Eero
 * Date: 6.10.2011
 * Time: 20:31
 * To change this template use File | Settings | File Templates.
 *
 * Find help at
 * http://www.playframework.org/documentation/1.2.3/guide2
 */

import models.ProblemCause;
import models.ProblemDefinition;
import models.RCACase;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;


public class PrototypeTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		new RCACase("Test RCA case.").save();
	}

	/*
		* Create new problem definitions for the RCA case. Try to search problem definitions and access case
		* information
		* from those objects. Select a problem definition to the second step of the RCA case.
		*
		* Problem definition:
		*   - title
		*   - case
		*   - causes <-- closest causes
		 */
	@Test
	public void problemDetection() {
		RCACase rcaCase = RCACase.find("byName", "Test RCA case.").first();
		new ProblemDefinition("Kahvi loppu", rcaCase);

		assertNotNull(ProblemDefinition.find("byTitle", "Kahvi loppu").first());
	}

	/*
		* Create new corrective actions for the root causes that were selected in the previous test. After
		* selecting some
		* of the corrective actions, finish the RCA case.
		*
		* CorrectiveAction
		*   - title
		*   - causes ManyToMany
		*   - problem
		 */
	@Test
	public void correctiveActionInnovation() {
		//TODO
	}
}
