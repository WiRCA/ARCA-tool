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

}