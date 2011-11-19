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

	@ManyToOne(cascade = CascadeType.ALL)
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

	@Transient
	public Set<Cause> causes;

	@Transient
	public Set<Cause> relations;

	@Transient
	public Cause parent;

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
	 * @param rcaCase
	 * @param name    name for the created cause.
	 * @param creator creator of the cause
	 *
	 * causes The set of causes that explain the cause.
	 * corrections List of corrections of the cause.
	 */
	public Cause(RCACase rcaCase, String name, User creator) {
		this.rcaCase = rcaCase;
		this.name = name;
		this.creatorId = creator.id;
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
	 * @return returns the Cause object.
	 */
	public Cause addCorrection(String name, String description) {
		Correction action = new Correction(name, description);
		action.save();
		this.corrections.add(action);
		this.save();
		return this;
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
		Cause newCause = new Cause(rcaCase, name, creator);
		Relation newRelation = new Relation(newCause, this);
		this.causeRelations.add(newRelation);
		newCause.effectRelations.add(newRelation);
		newCause.save();
		newRelation.save();
		this.save();
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
		newRelation.save();
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
			this.causeRelations.remove(relation);
		}
		for (Relation relation : effectRelations) {
			relation.causeTo.causeRelations.remove(relation);
			this.effectRelations.remove(relation);
		}
		this.save();
	}

	/**
	 * Gets the creator of the cause
	 *
	 * @return the creator of the cause
	 */
	public User getCreator() {
		return User.findById(creatorId);
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
		if (this.effectRelations.size() > 0) {
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
		return this.parent.equals(cause);
	}
}
