package models;

import play.Logger;
import play.db.jpa.Model;
import utils.EncodingUtils;

import javax.persistence.Entity;
import java.security.NoSuchAlgorithmException;

/**
 * @author Eero Laukkanen
 */

/**
 * TODO
 */
@Entity
public class User extends Model {

	public String email;
	public String name;
	public String password;

	//@ManyToMany
	//public Set<RCACase> cases;

	/**
	 * TODO
	 * @param email User's email address
	 * @param password User's password
	 */
	public User(String email, String password) {
		this.email = email;
		changePassword(password);
		//this.cases = new TreeSet<RCACase>();
	}

	/**
	 * Change user's password with new password
	 * @param newPassword User's new password
	 */
	public void changePassword(String newPassword) {
		try {
			this.password = EncodingUtils.encodeSHA1(newPassword);
		} catch (NoSuchAlgorithmException e) {
			// Should not happen
			Logger.error(e.getMessage(), "User's " + this.email + " password change failed");
		}
	}

	/**
	 * TODO
	 * @param name
	 * @param problem
	 *
	 * @return
	 */
	public User addRCACase(String name, String problem) {
		//TODO
		return this;
	}

}
