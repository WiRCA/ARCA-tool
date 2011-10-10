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
public class ProblemDefinition extends RCAGraphNode {

	public String name;

	@ManyToOne
	public RCACase rcaCase;

	public ProblemDefinition(String s, RCACase rcaCase) {
		this.name = s;
		this.rcaCase = rcaCase;
	}

}
