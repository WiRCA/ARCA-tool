import models.CorrectiveAction;
import models.ProblemCause;
import models.ProblemDefinition;
import models.RCACase;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

public class BasicTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
	}

	@Test
	public void createAndRetrieveRCACase() {
		// Create a new RCACase and save it
		new RCACase("Test case").save();

		RCACase testCase = RCACase.find("byName", "Test case").first();

		assertNotNull(testCase);
		assertEquals("Test case", testCase.name);
	}


	@Test
	public void goThroughRCACase() {
		// Create a new RCACase and save it
		RCACase testCase;
		testCase = new RCACase("Test case 2").save();
		ProblemDefinition problem = new ProblemDefinition("Test problem", testCase.id);
		ProblemCause cause = new ProblemCause("Test cause");
		CorrectiveAction action = new CorrectiveAction("Test action");

		assertEquals(RCACase.Status.ProblemDetection, testCase.status);
		testCase.nextStep();
		assertEquals(RCACase.Status.ProblemDetection, testCase.status);
		testCase.addProblemDefinition(problem);
		testCase.addSelectedProblemDefinition(problem);
		testCase.nextStep();
		assertEquals(RCACase.Status.RootCauseDetection, testCase.status);
		testCase.nextStep();
		assertEquals(RCACase.Status.RootCauseDetection, testCase.status);
		testCase.addSelectedProblemCause(cause);
		testCase.nextStep();

		assertEquals(RCACase.Status.CorrectiveActionInnovation, testCase.status);
		testCase.addSelectedCorrectiveAction(action);
		testCase.nextStep();
		assertEquals(RCACase.Status.Finished, testCase.status);
		testCase.nextStep();
		assertEquals(RCACase.Status.Finished, testCase.status);
	}

}
