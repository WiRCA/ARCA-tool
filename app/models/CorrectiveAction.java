package models;

import com.sun.jndi.ldap.ext.StartTlsResponseImpl;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Eero
 * Date: 6.10.2011
 * Time: 20:25
 * To change this template use File | Settings | File Templates.
 */

@Entity
public class CorrectiveAction extends Model {
	public String title;
	public ProblemDefinition problem;

	@ManyToMany
	public Set<ProblemCause> causes = new HashSet<ProblemCause>();

	public CorrectiveAction(String title, ProblemDefinition problem, Set<ProblemCause> causes){
		this.title = title;
		this.problem = problem;
		this.causes = causes;
	}

	public CorrectiveAction(String title, ProblemDefinition problem){
		this.title = title;
		this.problem = problem;
	}


	//MUST BE REDONE WHEN PROBLEMCAUSE IS FINISHED.

}
