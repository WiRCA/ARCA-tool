package models;

/**
 * Created by IntelliJ IDEA.
 * User: Eero
 * Date: 6.10.2011
 * Time: 19:59
 * To change this template use File | Settings | File Templates.
 */

import play.db.jpa.Model;

import javax.persistence.Entity;

@Entity
public class RCACase extends Model {
	public String name;

	public enum Status {
		ProblemDetection, RootCauseDetection, CorrectiveActionInnovation, Finished
	}

	public Status status;

	//@OneToMany(mappedBy="case", cascade=CascadeType.ALL)
	//public Set<ProblemDefinition> problems;

	public RCACase(String name) {
		this.name = name;
		this.status = models.RCACase.Status.ProblemDetection;
	}

	public boolean nextStep() {
		// TODO: add new class variables: selected problems, root causes and corrective actions. Require those
		// TODO: to be set before going to the next step in the RCA process.
		switch (status) {
			case ProblemDetection:
				status = Status.RootCauseDetection;
				return true;
			case RootCauseDetection:
				status = Status.CorrectiveActionInnovation;
				return true;
			case CorrectiveActionInnovation:
				status = Status.Finished;
				return true;
			default:
				return false;
		}
	}

}
