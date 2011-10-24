package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
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
public class User extends Model {

	public String email;
	public String name;
	public String password;

	//@ManyToMany
	//public Set<RCACase> cases;

	/**
	 * TODO
	 * @param email
	 * @param name
	 * @param password
	 */
	public User(String email, String name, String password) {
		this.email = email;
		this.name = name;
		this.password = password;
		//this.cases = new TreeSet<RCACase>();
	}

	/**
	 * TODO
	 * @param name
	 * @param problem
	 *
	 * @return
	 */
	public User addRCACase(String name, String problem) {
		//TODO
		return this;
	}

}
