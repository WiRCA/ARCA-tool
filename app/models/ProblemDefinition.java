package models;

import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ProblemDefinition extends RCAGraphNode {

	public String name;

	@ManyToOne
	public RCACase rcaCase;

	public ProblemDefinition(String name, long rcaCaseId) {
		this.name = name;
		this.rcaCase = models.RCACase.findById(rcaCaseId);
		this.rcaCase.addProblemDefinition(this);
	}

	public ProblemDefinition(String name) {
		this.name = name;
	}

}
