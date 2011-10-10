package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
        List<RCACase> RCACases = models.RCACase.find("order by Name").from(0).fetch(5);
        render(RCACases);
    }

	public static void show(long id) {
		models.RCACase rcaCase = models.RCACase.findById(id);
		Set<ProblemDefinition> Problems = rcaCase.problems;
		render(rcaCase);
	}

}