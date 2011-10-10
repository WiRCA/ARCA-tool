package models;

import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Eero
 * Date: 6.10.2011
 * Time: 20:24
 * To change this template use File | Settings | File Templates.
 */

@Entity
public class ProblemDefinition extends Model {

	public String name;

	@ManyToOne
	public RCACase rcaCase;

	@OneToMany(mappedBy = "problem", cascade = CascadeType.ALL)
	public Set<ProblemCause> causes = new HashSet<ProblemCause>();

	public ProblemDefinition(String s, RCACase rcaCase, Set<ProblemCause> causes) {
		this.name = s;
		this.rcaCase = rcaCase;
		this.causes = causes;
	}

	public ProblemDefinition(String s, RCACase rcaCase) {
		this(s, rcaCase, null);
	}

	public void addCause(ProblemCause cause){
		this.causes.add(cause);
	}
}
