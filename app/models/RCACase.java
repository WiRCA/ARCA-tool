package models;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Eero Laukkanen
 */


/**
 * TODO
 */
@Entity
@Table(name="rcacase")
public class RCACase extends Model {

	public String name;

	@OneToOne
	public Cause problem;

	/**
	 * TODO
	 * @param name todo
	 * @param problem todo
	 */
	public RCACase(String name, String problem) {
		this.name = name;
		this.problem = new Cause(problem).save();
	}
}
