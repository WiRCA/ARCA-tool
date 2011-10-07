import models.ProblemCause;
import models.ProblemDefinition;
import models.RCACase;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;


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
		ProblemDefinition probDef = new ProblemDefinition("Out of coffee", rcaCase);
		probDef.save();
		ProblemCause probCause = new ProblemCause("Out of coffee grounds");
		probCause.save();
		probDef.addCause(probCause);
		ProblemCause probCause2 = new ProblemCause("Out of money");
		probCause2.save();
		probCause.addCause(probCause2);
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
		probCause.hasCause(probCause2);
	}





}
