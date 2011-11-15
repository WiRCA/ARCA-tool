/*
 * Copyright (C) 2011 by Eero Laukkanen, Risto Virtanen, Jussi Patana, Juha Viljanen,
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

import play.db.jpa.Model;

import javax.persistence.*;
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

	public String hash;

	public String email;

	@ElementCollection
	@JoinTable(name = "invitationCases", joinColumns = {@JoinColumn(name = "invitationId", nullable = false)})
	@Column(name = "caseId")
	public Set<Long> caseIds;

	/**
	 * Creates invitation with email. Hash is generated automatically.
	 * @param email email where the invitation is send to.
	 */
	public Invitation(String email) {
		this.email = email;
		this.hash = "abcde1234"; //TODO
		this.caseIds = new HashSet<Long>();
	}

	/**
	 * Adds rights to an RCA case for the invitation.
	 * @param rcaCase
	 * @return
	 */
	public RCACase addCase(RCACase rcaCase) {
		this.caseIds.add(rcaCase.id);
		this.save();
		return rcaCase;
	}
}
