package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ProblemCause extends RCAGraphNode {

	public String name;

	@ManyToMany(mappedBy = "causes")
	public Set<CorrectiveAction> corrections = new HashSet<CorrectiveAction>();

	public ProblemCause(String name) {
		this.name = name;
	}

	public void addCorrection(CorrectiveAction action) {
		corrections.add(action);
		action.correctionTo(this);
	}
}
