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
		 */
	@Test
	public void problemDetection() {
		//TODO
	}

	/*
		* Create new problem causes for our RCA cases problem definition(s) that were selected in the previous step
		* . Create
		* more causes related to previously created causes, and then remove different causes. Select some of the root
		* causes for the next step of the RCA case.
		*
		 */
	@Test
	public void rootCauseDetection() {
		//TODO
	}

	/*
		* Create new corrective actions for the root causes that were selected in the previous test. After
		* selecting some
		* of the corrective actions, finish the RCA case.
		*
		 */
	@Test
	public void correctiveActionInnovation() {
		//TODO
	}
}
