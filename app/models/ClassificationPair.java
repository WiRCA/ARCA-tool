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

import utils.IdComparableModel;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * A representation of a child-parent classification relationship related to a cause - that is, a parent-child pair of
 * classifications.
 */
@PersistenceUnit(name = "maindb")
@Entity(name = "classificationpair")
public class ClassificationPair extends IdComparableModel {
	@ManyToOne
	public Classification parent;

	@ManyToOne
	public Classification child;

	public ClassificationPair(Classification parent, Classification child) {
		this.parent = parent;
		this.child = child;
	}

	public Classification getParent() {
		return parent;
	}

	public Classification getChild() {
		return child;
	}


	/**
	 * Finds or creates the pair from parent and child IDs.
	 * @param parentId the ID of the parent classification
	 * @param childId the ID of the child classification
	 * @return a ClassificationPair with the given parent and child IDs
	 */
	public static ClassificationPair createFromIds(Long parentId, Long childId) {
		// Try to return the existing one
		ClassificationPair existing = ClassificationPair.find(
				"SELECT c FROM classificationpair AS c WHERE parent.id=? and child.id=?",
				parentId, childId
		).first();
		if (existing != null) { return existing; }

		// Otherwise create a new one, store it and return it
		ClassificationPair out = new ClassificationPair((Classification) Classification.findById(parentId),
		                                                (Classification) Classification.findById(childId));
		out.save();
		return out;
	}


	/**
	 * Finds or creates the pair from parent and child classifications.
	 * @param parent the parent classification
	 * @param child the child classification
	 * @return a ClassificationPair with the given parent and child classifications
	 */
	public static ClassificationPair createFromClassifications(Classification parent, Classification child) {
		// Try to return the existing one
		ClassificationPair existing = ClassificationPair.find(
				"SELECT c FROM classificationpair AS c WHERE parent.id=? and child.id=?",
				parent.id, child.id
		).first();
		if (existing != null) { return existing; }

		// Otherwise create a new one, store it and return it
		ClassificationPair out = new ClassificationPair(parent, child);
		out.save();
		return out;
	}
}