package models;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * TODO
 *
 * @author Eero Laukkanen
 */

@PersistenceUnit(name = "maindb")
@Entity(name = "cause")
public class Cause extends Model {

	public String name;

	@OneToOne(mappedBy = "problem")
	public RCACase rcaCase;

	@Column(name = "creator_id")
	public Long creatorID;

	@ManyToMany
	@JoinTable(name = "causesof", joinColumns = {@JoinColumn(name = "effect_id", nullable = false)},
	           inverseJoinColumns = {@JoinColumn(name = "cause_id", nullable = false)})
	public Set<Cause> causes;

	@ElementCollection
	@JoinTable(name = "corrections", joinColumns = {@JoinColumn(name = "cause_id", nullable = false)})
	@Column(name = "correction")
	public List<String> corrections;

	/**
	 * Creates a new cause with name.
	 *
	 * @param name name for the created cause.
	 */
	public Cause(String name, User creator) {
		this.name = name;
		this.creatorID = creator.id;
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
		return this;
	}

	/**
	 * Adds a cause for a cause.
	 *
	 * @param name name to be used for the cause.
	 *
	 * @return cause the cause created when added.
	 */
	public Cause addCause(String name) {
		Cause newCause = new Cause(name, this.getCreator()).save();
		this.causes.add(newCause);
		return newCause;
	}

	/**
	 * Adds a cause for a cause. If another already cause exists, it should be added with this method.
	 *
	 * @param cause cause of this cause.
	 *
	 * @return itself.
	 */
	public Cause addCause(Cause cause) {
		//TODO
		return this;
	}

	public User getCreator() {
		return User.findById(creatorID);
	}
}
