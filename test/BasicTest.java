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
		assertEquals("Test case 2", testCase.name);
	}


	@Test
	public void goThroughRCACase() {
		// Create a new RCACase and save it
		RCACase testCase;
		testCase = new RCACase("Test case 2").save();

		assertEquals(RCACase.Status.ProblemDetection, testCase.status);
		testCase.nextStep();
		assertEquals(RCACase.Status.RootCauseDetection, testCase.status);
		testCase.nextStep();
		assertEquals(RCACase.Status.CorrectiveActionInnovation, testCase.status);
		testCase.nextStep();
		assertEquals(RCACase.Status.Finished, testCase.status);
		testCase.nextStep();
		assertEquals(RCACase.Status.Finished, testCase.status);
	}

}
