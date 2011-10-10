package models;

/**
 * Created by IntelliJ IDEA.
 * User: Eero
 * Date: 6.10.2011
 * Time: 19:59
 * To change this template use File | Settings | File Templates.
 */

import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
public class RCACase extends Model {
	public String name;

	public enum Status {
		ProblemDetection, RootCauseDetection, CorrectiveActionInnovation, Finished
	}

	public Status status;

	@OneToMany(mappedBy="rcaCase", cascade= CascadeType.ALL)
	public Set<ProblemDefinition> problems = new HashSet<ProblemDefinition>();

	@OneToMany
	public Set<ProblemDefinition> selectedProblems = new HashSet<ProblemDefinition>();

	@OneToMany
	public Set<ProblemCause> selectedCauses = new HashSet<ProblemCause>();

	@OneToMany
	public Set<CorrectiveAction> selectedActions = new HashSet<CorrectiveAction>();

	public RCACase(String name) {
		this.name = name;
		this.status = models.RCACase.Status.ProblemDetection;
		this.problems = new HashSet<ProblemDefinition>();
		this.selectedProblems = new HashSet<ProblemDefinition>();
		this.selectedCauses = new HashSet<ProblemCause>();
		this.selectedActions = new HashSet<CorrectiveAction>();
	}

	public boolean nextStep() {
		// TODO: add new class variables: selected problems, root causes and corrective actions. Require those
		// TODO: to be set before going to the next step in the RCA process.
		switch (status) {
			case ProblemDetection:
				if (this.selectedProblems.size() > 0) {
					status = Status.RootCauseDetection;
					return true;
				} else {
					return false;
				}
			case RootCauseDetection:
				if (this.selectedCauses.size() > 0) {
					status = Status.CorrectiveActionInnovation;
					return true;
				} else {
					return false;
				}
			case CorrectiveActionInnovation:
				if (this.selectedActions.size() > 0) {
					status = Status.Finished;
					return true;
				} else {
					return false;
				}
			default:
				return false;
		}
	}

	public void addProblemDefinition(ProblemDefinition problem){
		this.problems.add(problem);
		problem.rcaCase = this;
	}

	public void addSelectedProblemDefinition(ProblemDefinition problem){
		this.selectedProblems.add(problem);
	}

	public void addSelectedProblemCause(ProblemCause cause){
		this.selectedCauses.add(cause);
	}

	public void addSelectedCorrectiveAction(CorrectiveAction action){
		this.selectedActions.add(action);
	}



}
