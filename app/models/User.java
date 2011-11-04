package models;

import play.db.jpa.Model;
import utils.EncodingUtils;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a user in RCA application.
 * @author Eero Laukkanen
 */

/**
 * TODO
 */
@PersistenceUnit(name="userdb")
@Entity(name="user")
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
	 * @param rcaCase
	 * @return
	 */
	public RCACase addRCACase(RCACase rcaCase) {
		this.cases.add(rcaCase);
		return rcaCase;
	}

	/**
	 * TODO
	 * @param name
	 * @param type
	 * @param isMultinational
	 * @param companyName
	 * @param companySize
	 * @param isCasePublic
	 * @return
	 */
	public RCACase addRCACase(String name, String type, boolean isMultinational, String companyName,
	                          String companySize,
	               boolean isCasePublic){
		RCACase rcaCase = new RCACase(name, type, isMultinational, companyName, companySize, isCasePublic, this);
		this.cases.add(rcaCase);
		return rcaCase;
	}

}
