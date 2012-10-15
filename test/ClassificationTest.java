import models.Classification;
import models.RCACase;
import models.User;
import models.enums.ClassificationDimension;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created with IntelliJ IDEA.
 * User: jaffa
 * Date: 10/8/12
 * Time: 5:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassificationTest extends GenericRCAUnitTest {
	private String name;
	private String abbreviation;
	private String explanation;
	private ClassificationDimension classificationDimension;

	@Before
	public void setUp() {
		name = "Planning";

		abbreviation = "pla";
		explanation = "In planning phase.";
		classificationDimension = ClassificationDimension.valueOf(2);
	}

	@Test
	public void createClassificationTest() {
		super.setUp();
		Classification classification = new Classification(testCase, name, user,
		                                                   2, explanation, abbreviation);
		assertTrue(classification.name.equals(name));
		assertTrue(classification.creatorId.equals(user.getId()));
		assertTrue(classification.explanation.equals(explanation));
		assertTrue(classification.abbreviation.equals(abbreviation));
	}
}
