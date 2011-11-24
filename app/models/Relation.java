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

import utils.IdComparableModel;

import javax.persistence.*;

/**
 * Relation between two causes in an RCA tree.
 * @author Eero Laukkanen
 */

/**
 * TODO Timestamp for update information, updating said timestamp to RCACase
 */

@PersistenceUnit(name = "maindb")
@Entity(name = "relation")
public class Relation extends IdComparableModel {

	@ManyToOne
	@JoinColumn(name="causeFrom")
	public Cause causeFrom;

	@ManyToOne
	@JoinColumn(name="causeTo")
	public Cause causeTo;

	/**
	 * Creates a relation between two causes.
	 * @param causeFrom cause that is the cause of the relation
	 * @param causeTo cause that is the effect of the relation
	 */
	public Relation(Cause causeFrom, Cause causeTo) {
		this.causeFrom = causeFrom;
		this.causeTo = causeTo;
	}
}
