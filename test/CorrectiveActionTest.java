/**
 * Created by IntelliJ IDEA.
 * User: Pekka
 * Date: 7.10.2011
 * Time: 13.27
 * To change this template use File | Settings | File Templates.
 */

import models.ProblemCause;
import models.ProblemDefinition;
import models.RCACase;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

public class CorrectiveActionTest extends UnitTest {

 	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		new RCACase("Test RCA case.").save();

	}


	@Test
	public void correctiveAction() {
		RCACase rcaCase = RCACase.find("byName", "Test RCA case.").first();
		ProblemDefinition problem = new ProblemDefinition("Kahvi loppu", rcaCase);
		rcaCase.addProblemDefinition(problem);
		// TODO: Add corrective actions to ProblamCause.
	}
}
