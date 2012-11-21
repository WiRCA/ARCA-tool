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

import models.enums.StatusOfCause;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import utils.LikableIdComparableModel;

import javax.persistence.*;
import java.util.*;

/**
 * Cause in RCA case tree.
 *
 * @author Eero Laukkanen
 * @author AM Helin
 */
@PersistenceUnit(name = "maindb")
@Entity(name = "cause")
public class Cause extends LikableIdComparableModel {

	/**
	* the name of the cause
	*/
	public String name;

	/**
	 * the classifications of the cause
	 */
	@OneToMany(cascade = CascadeType.PERSIST, fetch=FetchType.EAGER) // eager to avoid reclassification problems
	@Sort(type = SortType.NATURAL)
	public SortedSet<ClassificationPair> classifications;

	/**
	* the RCA case that the cause belongs to
	*/
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "rcaCaseId")
	public RCACase rcaCase;

	/**
	* the id of the creator user
	*/
	public Long creatorId;

	/**
	* The updated date of the cause
	*/
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated", nullable = false)
	private Date updated;

	/**
	* Relations from the cause
	*/
	@OneToMany(mappedBy = "causeTo", cascade = CascadeType.ALL)
	@Sort(type = SortType.NATURAL)
	public SortedSet<Relation> causeRelations;

	/**
	* Relations to the cause
	*/
	@OneToMany(mappedBy = "causeFrom", cascade = CascadeType.ALL)
	@Sort(type = SortType.NATURAL)
	public SortedSet<Relation> effectRelations;

	/**
	* Corrective actions for the cause
	*/
	@OneToMany(mappedBy = "cause", cascade = CascadeType.ALL)
	@Sort(type = SortType.NATURAL)
	public SortedSet<Correction> corrections;

	/**
	* Status of the cause
	*/
	public Integer statusValue = StatusOfCause.DETECTED.value;

	/**
	* Relative X coordinate from the parent
	*/
	public Integer xCoordinate = 100;

	/**
	* Relative Y coordinate from the parent
	*/
	public Integer yCoordinate = 100;

	/**
	* Likes to the cause
	*/
	@ElementCollection
	@JoinTable(name = "causelikes", joinColumns = {@JoinColumn(name = "causeId", nullable = false)})
	@Column(name = "userId", nullable = false)
	public List<Long> likes;

	/**
	* This method is called when the cause is created
	*/
	@PrePersist
	protected void onCreate() {
		updated = new Date();
		rcaCase.updated = updated;
	}

	/**
	* This method is called when the cause is updated
	*/
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
	 *                <p/>
	 *                causes The set of causes that explain the cause.
	 *                corrections List of corrections of the cause.
	 */
	public Cause(RCACase rcaCase, String name, User creator) {
		this.rcaCase = rcaCase;
		this.name = name;
		if (creator != null) {
			this.creatorId = creator.id;
		}
		this.causeRelations = new TreeSet<Relation>();
		this.effectRelations = new TreeSet<Relation>();
		this.corrections = new TreeSet<Correction>();
		this.likes = new ArrayList<Long>();
		this.classifications = new TreeSet<ClassificationPair>();
	}


	/**
	 * Returns all classifications related to the case
	 * @return a set of classification pairs
	 */
	public SortedSet<ClassificationPair> getClassifications() {
		return this.classifications;
	}


	/**
	 * Sets the cause's classifications to the given ones.
	 * @param classifications the set of classification pairs to use
	 */
	public void setClassifications(SortedSet<ClassificationPair> classifications) {
		this.classifications = classifications;
	}


	/**
	 * Return users who have liked this cause.
	 *
	 * @return set of ids of users who have liked this cause
	 */
	public List<Long> getLikes() {
		return this.likes;
	}


	/**
	 * Adds a corrective action for a cause.
	 *
	 * @param name        name of the corrective action.
	 * @param description description of the corrective action.
	 *
	 * @return returns the created Correction object.
	 */
	public Correction addCorrection(String name, String description) {
		Correction correction = new Correction(name, description, this);
		correction.save();
		this.corrections.add(correction);
		this.save();
		return correction;
	}

	/**
	 * Removes the given correction from the cause corrections.
	 *
	 * @param correction correction to be removed
	 */
	public void removeCorrection(Correction correction) {
		this.corrections.remove(correction);
		correction.delete();
		this.save();
	}

	/**
	 * Adds a cause for a cause.
	 *
	 * @param name    name to be used for the cause.
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
	 *
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
	 * Returns the effect causes, none of which are the parent of this cause.
	 *
	 * @return the causes
	 */
	public Set<Cause> getRelations() {
		Set<Cause> relations = new TreeSet<Cause>();
		for (Relation relation : this.effectRelations) {
			if (!this.isChildOf(relation.causeTo)) {
				relations.add(relation.causeTo);
			}
		}
		return relations;
	}

	/**
	 * returns the parent of this cause.
	 *
	 * @return the parent of this cause
	 */
	public Cause getParent() {
		if (this.equals(this.rcaCase.problem)) {
			return null;
		} else if (this.effectRelations.size() > 0) {
			return ((Relation) this.effectRelations.toArray()[0]).causeTo;
		} else {
			return null;
		}
	}

	/**
	 * Checks if the cause is the parent of this cause.
	 *
	 * @param cause cause that can be the parent of this cause.
	 *
	 * @return true if the given cause is the parent of this cause.
	 */
	public boolean isChildOf(Cause cause) {
		Cause parent = this.getParent();
		return parent != null && parent.equals(cause);
	}

	/**
	 * Returns the status of the cause
	 *
	 * @return StatusOfCause enum is returned. Null is returned if not found.
	 */
	public StatusOfCause getStatus() {
		return StatusOfCause.valueOf(statusValue);
	}

	/**
	 * Set the status of the cause
	 *
	 * @param status the status to be set
	 */
	public void setStatus(StatusOfCause status) {
		this.statusValue = status.value;
	}

	/**
	* Basic toString method
	* @return the name, id and the rca case of the case
	*/
	@Override
	public String toString() {
		return name + " (id: " + id + ", rca case: " + rcaCase + ")";
	}

}
