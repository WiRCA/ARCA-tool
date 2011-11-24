import job.Bootstrap;
import models.Cause;
import models.RCACase;
import models.User;
import models.enums.CompanySize;
import models.enums.RCACaseType;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.EncodingUtils;

public class UserTest extends UnitTest {

	@Before
	public void setUp() {
		Fixtures.deleteAllModels();
		new Bootstrap().doJob();
	}

	@Test
	public void testPasswordChangeTest() {
		String userEmail = "userPassword@arcatool.fi";
		String userPassword = "password";
		new User(userEmail, userPassword).save();
		User normalUser = User.find("byEmailAndPassword", userEmail, EncodingUtils.encodeSHA1(userPassword)).first();
		assertNotNull(normalUser);
		assertEquals(normalUser.password, EncodingUtils.encodeSHA1(userPassword));
		normalUser.changePassword("newPassword");
		assertEquals(normalUser.password, EncodingUtils.encodeSHA1("newPassword"));
	}

	@Test
	public void addRcaCaseTest() {
		User rcaCaseUser = new User("rcaCaseUser@arcatool.fi", "password").save();
		assertNotNull(rcaCaseUser);
		RCACase anotherRCACase = new RCACase("new unique rca case", RCACaseType.HR.value, "Kaapelissa ei vikaa",
				            "Kaapelissa vikaa", true, "test company", CompanySize.FIFTY.value, "Kaikenlaista romua",
				            true, rcaCaseUser);
		rcaCaseUser.addRCACase(anotherRCACase);
		assertTrue(rcaCaseUser.caseIds.size() == 1);
		assertTrue(rcaCaseUser.getRCACases().size() == 1);
		RCACase rcaCase = RCACase.find("byName", "new unique rca case").first();
		assertNotNull(rcaCase);
		assertTrue(rcaCaseUser.caseIds.contains(rcaCase.id));
		assertTrue(rcaCaseUser.getRCACases().contains(rcaCase));
		assertTrue(rcaCase.getOwner().equals(rcaCaseUser));
		assertTrue(rcaCase.problem.name.equals("new unique rca case"));
	}

	@Test
	public void removeRcaCaseTest() {
		User user = new User ("test@test.fi", "password");
				RCACase anotherRCACase = new RCACase("new unique rca case", RCACaseType.HR.value, "Kaapelissa ei vikaa",
				            "Kaapelissa vikaa", true, "test company", CompanySize.FIFTY.value, "Kaikenlaista romua",
				            true, user).save();
				RCACase anotherRCACase2 = new RCACase("new unique rca case", RCACaseType.HR.value,
				                                      "Kaapelissa ei vikaa",
				            "Kaapelissa vikaa", true, "test company", CompanySize.FIFTY.value, "Kaikenlaista romua",
				            true, user).save();

		anotherRCACase2.problem = new Cause(anotherRCACase2, "test cause", user);
		user.addRCACase(anotherRCACase);
		user.addRCACase(anotherRCACase2);

		assertTrue(user.getRCACases().contains(anotherRCACase));

		user.removeRCACase(anotherRCACase);

		assertFalse(user.getRCACases().contains(anotherRCACase));

		assertTrue(user.getRCACases().contains(anotherRCACase2));
	}

}
