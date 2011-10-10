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

	public ProblemDefinition(String s, RCACase rcaCase) {
		this.name = s;
		this.rcaCase = rcaCase;
	}

	public ProblemDefinition(String s) {
		this.name = s;
	}

}
