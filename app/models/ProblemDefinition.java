package models;

import play.db.jpa.Model;

import javax.persistence.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: Eero
 * Date: 6.10.2011
 * Time: 20:24
 * To change this template use File | Settings | File Templates.
 */

@Entity
public class ProblemDefinition extends Model {
	public ProblemDefinition(String s, RCACase rcaCase) {
	}

	public void addCause(ProblemCause probCause) {
	}
}
