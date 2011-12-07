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
import java.util.*;

/**
 * Cause in RCA case tree.
 *
 * @author Eero Laukkanen
 */

@PersistenceUnit(name = "maindb")
@Entity(name = "cause")
public class Cause extends IdComparableModel {

	public String name;

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "rcaCaseId")
	public RCACase rcaCase;

	public Long creatorId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated", nullable = false)
	private Date updated;


	@OneToMany(mappedBy = "causeTo", cascade = CascadeType.ALL)
	public Set<Relation> causeRelations;

	@OneToMany(mappedBy = "causeFrom", cascade = CascadeType.ALL)
	public Set<Relation> effectRelations;

	@OneToMany(mappedBy = "cause", cascade = CascadeType.ALL)
	public Set<Correction> corrections;

	@PrePersist
	protected void onCreate() {
		updated = new Date();
		rcaCase.updated = updated;
	}

	@PreUpdate
	protected void onUpdate() {
		updated = new Date();
		rcaCase.updated = updated;
	}

	/**
	 * Creates a new cause with name and creator.
	 *
	 * @param rcaCase RCA case which this cause belongs to
	 * @param name    name for the created cause.
	 * @param creator creator of the cause
	 *
	 * causes The set of causes that explain the cause.
	 * corrections List of corrections of the cause.
	 */
	public Cause(RCACase rcaCase, String name, User creator) {
		this.rcaCase = rcaCase;
		this.name = name;
		if (creator != null) {
			this.creatorId = creator.id;
		}
		causeRelations = new TreeSet<Relation>();
		effectRelations = new TreeSet<Relation>();
		corrections = new TreeSet<Correction>();
	}

	/**
	 * Adds a corrective action for a cause.
	 *
	 * @param name name of the corrective action.
	 * @param description description of the corrective action.
	 *
	 * @return returns the created Correction object.
	 */
	public Correction addCorrection(String name, String description) {
		Correction action = new Correction(name, description, this);
	  action.save();
		this.corrections.add(action);
		this.save();
		return action;
	}

	/**
	 * Removes the given correction from the cause corrections.
	 * @param correction correction to be removed
	 */
	public void removeCorrection(Correction correction) {
		this.corrections.remove(correction);
		correction.delete();
		this.save();
	}
	
	public TreeSet<Correction> getCorrections() {
	  TreeSet<Correction> corrections = new TreeSet<Correction>();
		for (Correction correction : this.corrections) {
		  corrections.add(correction);
		}
		return corrections;
	}

	/**
	 * Adds a cause for a cause.
	 *
	 * @param name name to be used for the cause.
	 *
	 * @param creator creator of the cause
	 *
	 * @return cause the cause created when added.
	 */
	public Cause addCause(String name, User creator) {
		Cause newCause = new Cause(rcaCase, name, creator).save();
		Relation newRelation = new Relation(newCause, this);
		this.causeRelations.add(newRelation);
		newCause.effectRelations.add(newRelation);
		newCause.save();
		newRelation.save();
		this.save();
		this.rcaCase.causes.add(newCause);
		this.rcaCase.save();
		return newCause;
	}

	/**
	 * Adds a cause for a cause. If another already cause exists, it should be added with this method.
	 *
	 * @param cause cause to add.
	 *
	 * @return returns the added cause
	 */
	public Cause addCause(Cause cause) {
		Relation newRelation = new Relation(cause, this);
		this.causeRelations.add(newRelation);
		cause.effectRelations.add(newRelation);
		cause.save();
		this.save();
		return cause;
	}

	/**
	 * Deletes the cause from the relations of other causes.
	 *
	 */
	public void deleteCause() {
		for (Relation relation : causeRelations) {
			relation.causeFrom.effectRelations.remove(relation);
		}
		for (Relation relation : effectRelations) {
			relation.causeTo.causeRelations.remove(relation);
		}
	}

	/**
	 * Gets the creator of the cause
	 *
	 * @return the creator of the cause
	 */
	public User getCreator() {
		return creatorId != null ? (User) User.findById(creatorId) : null;
	}

	/**
	 * Returns the children of this cause.
	 * @return the children of this cause.
	 */
	public Set<Cause> getCauses() {
		Set<Cause> children = new TreeSet<Cause>();
		for (Relation relation : this.causeRelations) {
			if (relation.causeFrom.isChildOf(this)) {
				children.add(relation.causeFrom);
			}
		}
		return children;
	}

	/**
	 * Returns the relations to the causes that are not children of this cause.
	 * @return the relations
	 */
	public Set<Cause> getRelations() {
		Set<Cause> relations = new TreeSet<Cause>();
		for (Relation relation : this.causeRelations) {
			if (!relation.causeFrom.isChildOf(this)) {
				relations.add(relation.causeFrom);
			}
		}
		return relations;
	}

	/**
	 * returns the parent of this cause.
	 * @return the parent of this cause
	 */
	public Cause getParent() {
		if (this.equals(this.rcaCase.problem)) {
			return null;
		}
		else if (this.effectRelations.size() > 0) {
			return ((Relation)this.effectRelations.toArray()[0]).causeTo;
		}
		else {
			return null;
		}
	}

	/**
	 * Checks if the cause is the parent of this cause.
	 * @param cause cause that can be the parent of this cause.
	 * @return true if the given cause is the parent of this cause.
	 */
	public boolean isChildOf(Cause cause) {
		Cause parent = this.getParent();
		return parent != null && parent.equals(cause);
	}

	@Override
	public String toString() {
		return name + " (id: " + id + ", rca case: " + rcaCase + ")";
	}
}
