package models;

import com.sun.jndi.ldap.ext.StartTlsResponseImpl;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
public class CorrectiveAction extends Model {
	public String title;

	@ManyToMany
	public Set<ProblemCause> causes = new HashSet<ProblemCause>();

	public CorrectiveAction(String title, Set<ProblemCause> causes){
		this.title = title;
		this.causes = causes;
	}

	public CorrectiveAction(String title) {
		this.title = title;
	}

	public void correctionTo(ProblemCause cause) {
		this.causes.add(cause);
	}




	//MUST BE REDONE WHEN PROBLEMCAUSE IS FINISHED.

}
