package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Eero Laukkanen
 */


/**
 * TODO
 */
@Entity
public class RCACase extends Model {

	public String name;
	public String problem;

	@OneToMany
	public Set<Cause> causes;

	/**
	 * TODO
	 * @param name
	 * @param problem
	 */
	public RCACase(String name, String problem) {
		this.name = name;
		this.problem = problem;
		this.causes = new TreeSet<Cause>();
	}

	/**
	 * TODO
	 * @param name
	 *
	 * @return
	 */
	public RCACase addCause(String name) {
		//TODO
		return this;
	}
}
