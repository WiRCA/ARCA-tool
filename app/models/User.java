package models;

import play.data.validation.Email;
import play.data.validation.Password;
import play.db.jpa.Model;
import utils.EncodingUtils;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a user in RCA application.
 * @author Eero Laukkanen
 */
@PersistenceUnit(name = "userdb")
@Entity(name = "user")
public class User extends Model {

	@Email
	public String email;

	public String name;

	@Password
	public String password;

	@ElementCollection
	@JoinTable(name = "usercases", joinColumns = {@JoinColumn(name = "userId", nullable = false)})
	@Column(name = "caseId", nullable = false)
	public Set<Long> caseIds;

	/**
	 * @param email    User's email address
	 * @param password User's password
	 *
	 * caseIds The set of IDs of all the private RCA cases that the User can see.
	 */
	public User(String email, String password) {
		this.email = email;
		this.password = EncodingUtils.encodeSHA1(password);
		this.caseIds = new HashSet<Long>();
	}

	/**
	 * Change user's password with new password
	 *
	 * @param newPassword User's new password
	 */
	public void changePassword(String newPassword) {
		this.password = EncodingUtils.encodeSHA1(newPassword);
	}

	/**
	 * @param rcaCase The RCA case to be added to the User.
	 *
	 * @return RCACase object that represents the existing RCA case that is added to User.
	 */
	public RCACase addRCACase(RCACase rcaCase) {
		if (rcaCase.problem == null) {
			// Creating the new 'initial problem' for the RCACase with the case name.
			rcaCase.problem = new Cause(rcaCase, rcaCase.caseName, this).save();
			rcaCase.save();
		}
		this.caseIds.add(rcaCase.id);
		this.save();
		return rcaCase;
	}

	/**
	 * Removes RCACase from the cases of the user
	 * @param rcaCase the RCA case to be removed
	 */
	public void removeRCACase(RCACase rcaCase) {
		this.caseIds.remove(rcaCase.id);
		this.save();
	}

	public Set<RCACase> getRCACases() {
		HashSet<RCACase> cases = new HashSet<RCACase>();
		for (Long id : caseIds) {
			RCACase rcaCase = RCACase.findById(id);
			cases.add(rcaCase);
		}
		return cases;
	}

	@Override
	public String toString() {
		return name + " (" + id + ", " + email + ")";
	}
}
