/*
 * Copyright (C) 2012 by Eero Laukkanen, Risto Virtanen, Jussi Patana, Juha Viljanen,
 * Joona Koistinen, Pekka Rihtniemi, Mika Kekäle, Roope Hovi, Mikko Valjus,
 * Timo Lehtinen, Jaakko Harjuhahto
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package models;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import play.data.validation.*;
import play.db.jpa.Model;
import utils.EncodingUtils;

import javax.persistence.*;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class represents a user in RCA application.
 *
 * @author Eero Laukkanen
 */
@PersistenceUnit(name = "userdb")
@Entity(name = "user")
public class User extends Model {

	/**
	* The email of the user
	*/
	@Required
	@Email
	@MaxSize(value = 255)
	public String email;

	/**
	* The name of the user
	*/
	@Required
	@MaxSize(value = 64)
	@MinSize(value = 1)
	public String name;

	/**
	* The password of the user
	* Saved to the database using SHA-1
	*/
	@Required
	@Password
	public String password;

	/**
	* Ids of the cases that the user has rights to
	*/
	@ElementCollection
	@JoinTable(name = "usercases", joinColumns = {@JoinColumn(name = "userId", nullable = false)})
	@Column(name = "caseId", nullable = false)
	@Sort(type = SortType.NATURAL)
	public SortedSet<Long> caseIds;

	/**
	 * @param email    User's email address
	 * @param password User's password
	 *                 <p/>
	 *                 caseIds The set of IDs of all the private RCA cases that the User can see.
	 */
	public User(String email, String password) {
		this.email = email;
		this.password = EncodingUtils.encodeSHA1(password);
		this.caseIds = new TreeSet<Long>();
	}

	/**
	 * Basic constructor
	 */
	public User() {
		this.caseIds = new TreeSet<Long>();
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
	 * @param problemName Name of the problem that appears as the root node in the cause-effect tree.
	 *
	 * @return RCACase object that represents the existing RCA case that is added to User.
	 */
	public RCACase addRCACase(RCACase rcaCase, String problemName) {
		if (rcaCase.problem == null) {
			// Creating the new 'initial problem' for the RCACase with the problem name.
			rcaCase.problem = new Cause(rcaCase, problemName, this).save();
			rcaCase.save();
		}
		this.caseIds.add(rcaCase.id);
		this.save();
		return rcaCase;
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
	 *
	 * @param rcaCase the RCA case to be removed
	 */
	public void removeRCACase(RCACase rcaCase) {
		this.caseIds.remove(rcaCase.id);
		this.save();
	}

	/**
	 * Return cases that this user has created or has been invited.
	 *
	 * @return sorted set of the cases
	 */
	public SortedSet<RCACase> getRCACases() {
		TreeSet<RCACase> cases = new TreeSet<RCACase>();
		for (Long id : caseIds) {
			RCACase rcaCase = RCACase.findById(id);
			cases.add(rcaCase);
		}
		return cases;
	}

	/**
	* Basic toString method
	* @return the name, id, and email of the user
	*/
	@Override
	public String toString() {
		return name + " (" + id + ", " + email + ")";
	}
}
