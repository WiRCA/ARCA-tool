package models;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * TODO
 * @author Eero Laukkanen
 */

@Entity
@Table(name="cause")
public class Cause extends Model {

	public String name;

	@ManyToOne
	public RCACase rcaCase;

	@ManyToMany
	@JoinTable(name="causesof", joinColumns = {@JoinColumn(name = "id_effect", nullable = false)},
	inverseJoinColumns = {@JoinColumn(name="id_cause", nullable = false)})
	public Set<Cause> causes;

	@JoinTable(name="corrections", joinColumns = {@JoinColumn(name="id_cause", nullable = false)})
	public ArrayList<String> corrections;

	/**
	 * Creates a new cause with name.
	 * @param name name for the created cause.
	 */
	public Cause(String name) {
		this.name = name;
		causes = new TreeSet<Cause>();
		corrections = new ArrayList<String>();
	}

	/**
	 * Adds a corrective action for a cause.
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
	 * @param name name to be used for the cause.
	 *
	 * @return itself.
	 */
	public Cause addCause(String name) {
		//TODO
		return this;
	}

	/**
	 * Adds a cause for a cause. If another already cause exists, it should be added with this method.
	 * @param cause cause of this cause.
	 * @return itself.
	 */
	public Cause addCause(Cause cause) {
		//TODO
		return this;
	}
}
