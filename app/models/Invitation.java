/*
 * Copyright (C) 2011 - 2013 by Eero Laukkanen, Risto Virtanen, Jussi Patana,
 * Juha Viljanen, Joona Koistinen, Pekka Rihtniemi, Mika Kekäle, Roope Hovi,
 * Mikko Valjus, Timo Lehtinen, Jaakko Harjuhahto, Jonne Viitanen, Jari Jaanto,
 * Toni Sevenius, Anssi Matti Helin, Jerome Saarinen, Markus Kere
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

import play.Logger;
import play.db.jpa.Model;

import javax.persistence.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Invitation to an RCA case. Invitation can be consumed by visiting UserController.registerUserWithInvitation.
 *
 * @author Eero Laukkanen
 */
@PersistenceUnit(name="userdb")
@Entity(name="invitation")
public class Invitation extends Model {

    private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";
	private static final int HASH_VALUE_LENGTH = 10;

	/**
	* The unique hash of the invitation
	* Hash is inserted to the link that is emailed to the invited user.
	* With the hash the user can be authorized
	*/
	public String hash;

	/**
	* the email to be invited
	*/
	public String email;

	/**
	* The ids of the rca cases that the invitation has rights for
	*/
	@ElementCollection
	@JoinTable(name = "invitationCases", joinColumns = {@JoinColumn(name = "invitationId", nullable = false)})
	@Column(name = "caseId")
	public Set<Long> caseIds;

	/**
	 * Creates invitation with email. Hash is generated automatically.
	 * @param email email where the invitation is send to.
	 */
	public Invitation(String email) {
		try {
			this.email = email;
			SecureRandom secureRandom = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM);
			this.hash = Integer.toHexString(secureRandom.nextInt()).toUpperCase();
			while (this.hash.length() < HASH_VALUE_LENGTH) {
				this.hash = "0" + this.hash;
			}
			this.hash = this.hash.substring(0, HASH_VALUE_LENGTH);
			this.caseIds = new HashSet<Long>();
		} catch (NoSuchAlgorithmException e) {
			// Should not happen
			Logger.error(e, "Invitation hash generation failed");
		}
	}

	/**
	 * Adds rights to an RCA case for the invitation.
	 * @param rcaCase RCA case which for rights are added
	 * @return Added RCA case
	 */
	public RCACase addCase(RCACase rcaCase) {
		this.caseIds.add(rcaCase.id);
		this.save();
		return rcaCase;
	}

	/**
	 * Removes RCACase from the cases of the invitation
	 * @param rcaCase the RCA case to be removed
	 */
	public void removeRCACase(RCACase rcaCase) {
		this.caseIds.remove(rcaCase.id);
		this.save();
	}

	/**
	* Basic toString method
	* @return email of the invitation
	*/
	@Override
	public String toString() {
		return "Invitation{" + "email='" + email + '\'' + '}';
	}
}
