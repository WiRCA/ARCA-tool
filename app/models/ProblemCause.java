package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by IntelliJ IDEA.
 * User: Eero
 * Date: 6.10.2011
 * Time: 20:24
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class ProblemCause extends RCAGraphNode {

	public String name;

	@ManyToOne
	public ProblemDefinition problem;

	public ProblemCause(String name) {
		this.name = name;
	}
}
