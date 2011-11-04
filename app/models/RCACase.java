package models;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Eero Laukkanen
 */


/**
 * TODO   ENUMS
 */
@Entity(name = "rcacase")
@PersistenceUnit(name="maindb")
public class RCACase extends Model {

	public String name;
	public TreeSet<Cause> causes;
	public String caseType;
	public boolean isMultinational;
	public String companyName;
	// Company size could (should?) be implemented in the future as Enum.
	public String companySize;
	public boolean isCasePublic;

	@ManyToOne
	public User owner;

	@OneToOne
	public Cause problem;

	/**
	 * Constructor for the form in create.html.
	 *
	 * @param name
	 * @param type
	 * @param isMultinational
	 * @param companyName
	 * @param companySize
	 * @param isCasePublic
	 */

	public RCACase(String name, String type, boolean isMultinational, String companyName, String companySize,
	               boolean isCasePublic, User owner) {

		this.name = name;
		this.caseType = type;
		this.isMultinational = isMultinational;
		this.companyName = companyName;
		this.companySize = companySize;
		this.isCasePublic = isCasePublic;
		this.owner = owner;
		this.causes = new TreeSet<Cause>();
		// Creating the new 'initial problem' for the RCACase with the case name.
		this.problem = new Cause(name, owner).save();

		//TODO Rest of the parameters
	}

}
