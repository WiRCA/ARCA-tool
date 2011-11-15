package models;

import org.hibernate.annotations.Cascade;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Cause in RCA case tree.
 *
 * @author Eero Laukkanen
 */

@PersistenceUnit(name = "maindb")
@Entity(name = "cause")
public class Cause extends Model {

	public String name;

	@ManyToOne
	@JoinColumn(name = "rcaCaseId")
	public RCACase rcaCase;

	public Long creatorId;

	@OneToMany(mappedBy = "causeTo", cascade = CascadeType.PERSIST)
	public Set<Relation> effects;

	@OneToMany(mappedBy = "causeFrom", cascade = CascadeType.PERSIST)
	public Set<Relation> causes;

	@OneToMany(mappedBy = "cause", cascade = CascadeType.PERSIST)
	public Set<Correction> corrections;

	/**
	 * Creates a new cause with name and creator.
	 *
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
		causes = new TreeSet<Relation>();
		effects = new TreeSet<Relation>();
		corrections = new TreeSet<Correction>();
	}

	/**
	 * Deprecated method, use addCorrection(name, description) instead.
	 * Adds a corrective action for a cause.
	 *
	 * @param name name of the corrective action.
	 *
	 * @return returns the Cause object.
	 */
	@Deprecated
	public Cause addCorrection(String name) {
		Correction action = new Correction(name, "");
		action.save();
		this.corrections.add(action);
		this.save();
		return this;
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
		this.causes.add(newRelation);
		newCause.effects.add(newRelation);
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
		this.causes.add(newRelation);
		cause.effects.add(newRelation);
		cause.save();
		newRelation.save();
		this.save();
		return cause;
	}

	/**
	 * Deletes the cause.
	 *
	 */
	public void deleteCause() {
		for (Relation relation : causes) {
			relation.causeFrom.effects.remove(relation);
			relation.causeFrom.save();
			relation.delete();
		}
		for (Relation relation : effects) {
			relation.causeTo.causes.remove(relation);
			relation.causeTo.save();
			relation.delete();
		}
		this.delete();
	}

	/**
	 * Gets the creator of the cause
	 *
	 * @return the creator of the cause
	 */
	public User getCreator() {
		return User.findById(creatorId);
	}
}
