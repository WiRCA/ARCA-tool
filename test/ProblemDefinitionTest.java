/**
 * Created by IntelliJ IDEA.
 * User: Mikko
 * Date: 7.10.2011
 * Time: 12:23
 * To change this template use File | Settings | File Templates.
 */

import models.ProblemCause;
import models.ProblemDefinition;
import models.RCACase;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

public class ProblemDefinitionTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		new RCACase("Test RCA case.").save();

	}

	@Test
	public void ProblemDetect() {
		RCACase rcaCase = RCACase.find("byName", "Test RCA case.").first();
		ProblemDefinition problem = new ProblemDefinition("Kahvi loppu", rcaCase).save();
		rcaCase.addProblemDefinition(problem);
		assertTrue(rcaCase.problems.contains(problem));
		assertNotNull(ProblemDefinition.find("byName", "Kahvi loppu").first());
	}

	@Test
	public void CauseAdding() {
		RCACase rcaCase = RCACase.find("byName", "Test RCA case.").first();
		ProblemCause cause = new ProblemCause("Ei printattu");
		ProblemDefinition problem1 = new ProblemDefinition("Ei puruja", rcaCase);
		problem1.addCause(cause);
		assertTrue(cause.isCauseOf(problem1));
	}
}
