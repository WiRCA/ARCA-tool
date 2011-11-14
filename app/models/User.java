package models;

import models.enums.CompanySize;
import models.enums.RCACaseType;
import play.data.validation.Valid;
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

	public String email;
	public String name;
	public String password;

	@ElementCollection
	@JoinTable(name = "usercases", joinColumns = {@JoinColumn(name = "user_id", nullable = false)})
	@Column(name = "case_id")
	public Set<Long> caseIDs;

	/**
	 * @param email    User's email address
	 * @param password User's password
	 *
	 * caseIDs The set of IDs of all the private RCA cases that the User can see.
	 */
	public User(String email, String password) {
		this.email = email;
		this.password = EncodingUtils.encodeSHA1(password);
		this.caseIDs = new HashSet<Long>();
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
		this.caseIDs.add(rcaCase.id);
		this.save();
		return rcaCase;
	}

	/**
	 * @param caseName            The name of the RCA case
	 * @param type            The type of the RCA case. Enums are found in models/enums/RCACaseType.
	 * @param isMultinational The boolean value whether the company related to the RCA case is multinational.
	 * @param companyName     The name of the company related to the RCA case.
	 * @param companySizeValue     The size of the company related to the RCA case. Enums are found in
	 * models/enums/CompanySize.
	 * @param description
	 * @param companyProducts
	 * @param isCasePublic    The boolean value whether the RCA is public.
	 *
	 * @return RCACase object that represents the created RCA case added to the User.
	 */
	public RCACase addRCACase(@Valid String caseName, @Valid int type, @Valid String caseGoals,
	                          @Valid String description,
	                          boolean isMultinational,
	               @Valid String companyName,
	               @Valid int companySizeValue, @Valid String companyProducts, boolean isCasePublic) {
		RCACase rcaCase = new RCACase(caseName, type, caseGoals, description, isMultinational, companyName,
		                              companySizeValue,
		                              companyProducts,
		                              isCasePublic,
		                              this).save();
		// Creating the new 'initial problem' for the RCACase with the case name.
		rcaCase.problem = new Cause(rcaCase, caseName, this).save();
		rcaCase.save();
		this.caseIDs.add(rcaCase.id);
		this.save();
		return rcaCase;
	}

	public Set<RCACase> getRCACases() {
		HashSet<RCACase> cases = new HashSet<RCACase>();
		for (Long id : caseIDs) {
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
