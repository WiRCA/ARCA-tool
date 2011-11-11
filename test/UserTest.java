import models.RCACase;
import models.User;
import models.enums.CompanySize;
import models.enums.RCACaseType;
import org.junit.Test;
import play.mvc.Before;
import play.test.UnitTest;
import utils.EncodingUtils;

public class UserTest extends UnitTest {

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
		rcaCaseUser.addRCACase("new unique rca case", RCACaseType.HR.value, true, "test company",
		                       CompanySize.FIFTY.value, true).save();
	    rcaCaseUser.save();
	    rcaCaseUser.refresh();
	    assertTrue(rcaCaseUser.caseIDs.size() == 1);
	    assertTrue(rcaCaseUser.getRCACases().size() == 1);
	    RCACase rcaCase = RCACase.find("byName", "new unique rca case").first();
	    assertNotNull(rcaCase);
	    assertTrue(rcaCaseUser.caseIDs.contains(rcaCase.id));
	    assertTrue(rcaCaseUser.getRCACases().contains(rcaCase));
	    assertTrue(rcaCase.getOwner().equals(rcaCaseUser));
	    assertTrue(rcaCase.problem.name.equals("new unique rca case"));
    }

}
