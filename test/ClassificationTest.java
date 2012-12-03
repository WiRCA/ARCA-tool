import models.Classification;
import models.ClassificationDimension;
import org.junit.Before;
import org.junit.Test;

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
		classificationDimension = ClassificationDimension.valueOf(ClassificationDimension.WHERE_DIMENSION_ID);
	}

	@Test
	public void createClassificationTest() {
		super.setUp();
		Classification classification = new Classification(testCase, name, user,
		                                                   ClassificationDimension.WHERE_DIMENSION_ID,
		                                                   explanation, abbreviation);
		assertTrue(classification.name.equals(name));
		assertTrue(classification.creatorId.equals(user.getId()));
		assertTrue(classification.explanation.equals(explanation));
		assertTrue(classification.abbreviation.equals(abbreviation));
	}
}
