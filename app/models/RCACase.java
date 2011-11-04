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
 * TODO   ENUMS
 */
@Entity
public class RCACase extends Model {

	public String name;
	public String problem;
	public Enum type;
	public boolean isMultinational;
	public String companyName;
	public Enum companySize;
	public boolean isPublic;


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
		//TODO Rest of the parameters
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
