package models;

import play.db.jpa.Model;
import utils.EncodingUtils;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Eero Laukkanen
 */

/**
 * TODO
 */
@Entity
@Table(name="user")
@PersistenceUnit(name="userdb")
public class User extends Model {

	public String email;
	public String name;
	public String password;

	@ManyToMany
	@JoinTable(name="usercases", joinColumns = {@JoinColumn(name="user_id", nullable = false)},
	           inverseJoinColumns = {@JoinColumn(name="case_id", nullable = false)})
	public Set<RCACase> cases;

	/**
	 * TODO
	 * @param email User's email address
	 * @param password User's password
	 */
	public User(String email, String password) {
		this.email = email;
		this.password = EncodingUtils.encodeSHA1(password);
		this.cases = new HashSet<RCACase>();
	}

	/**
	 * Change user's password with new password
	 * @param newPassword User's new password
	 */
	public void changePassword(String newPassword) {
		this.password = EncodingUtils.encodeSHA1(newPassword);
	}

	/**
	 * TODO
	 * @param name todo
	 * @param problem todo
	 *
	 * @return user object
	 */
	public User addRCACase(String name, String problem) {
		RCACase rcaCase = new RCACase(name, problem).save();
		this.cases.add(rcaCase);
		return this;
	}

}
