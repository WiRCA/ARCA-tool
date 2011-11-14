package models;

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
	@JoinColumn(name = "rcacase_id")
	public RCACase rcaCase;

	@Column(name = "creator_id")
	public Long creatorId;

	@ManyToMany
	@JoinTable(name = "causesof", joinColumns = {@JoinColumn(name = "effect_id", nullable = false)},
	           inverseJoinColumns = {@JoinColumn(name = "cause_id", nullable = false)})
	public Set<Cause> causes;

	@ManyToMany(mappedBy = "causes")
	public Set<Cause> causesOf;

	@ElementCollection
	@JoinTable(name = "corrections", joinColumns = {@JoinColumn(name = "cause_id", nullable = false)})
	@Column(name = "correction")
	public List<String> corrections;

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
		causes = new TreeSet<Cause>();
		corrections = new ArrayList<String>();
	}

	/**
	 * Adds a corrective action for a cause.
	 *
	 * @param name name of the corrective action.
	 *
	 * @return returns the Cause object.
	 */
	public Cause addCorrection(String name) {
		this.corrections.add(name);
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
		Cause newCause = new Cause(rcaCase, name, creator).save();
		this.causes.add(newCause);
		this.save();
		return newCause;
	}

	/**
	 * Adds a cause for a cause. If another already cause exists, it should be added with this method.
	 *
	 * @param cause cause to add.
	 *
	 * @return on success returns the added cause, otherwise returns null
	 */
	public Cause addCause(Cause cause) {
		this.causes.add(cause);
		this.save();
		return cause;
	}

	/**
	 * Delete a cause from a cause.
	 *
	 */
	public void deleteCause() {
		for (Cause cause : causesOf) {
			cause.causes.remove(this);
			cause.save();
		}
		this.rcaCase = null;
		this.delete();
	}

	/**
	 * Get the creator of the cause
	 *
	 * @return the creator of the cause
	 */
	public User getCreator() {
		return User.findById(creatorId);
	}
}
