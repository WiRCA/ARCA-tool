/*
 * Copyright (C) 2011 by Eero Laukkanen, Risto Virtanen, Jussi Patana, Juha Viljanen, Joona Koistinen, Pekka Rihtniemi, Mika Kekäle, Roope Hovi, Mikko Valjus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package controllers;

import models.ProblemDefinition;
import play.mvc.Controller;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Pekka
 * Date: 10.10.2011
 * Time: 18.33
 * To change this template use File | Settings | File Templates.
 */

public class RCACase extends Controller {

	    public static void index() {
		List<RCACase> RCACases = models.RCACase.find("order by Name").from(0).fetch(10);
        render(RCACases);
    }
		public static void show(long id) {
		models.RCACase rcaCase = models.RCACase.findById(id);
		Set<ProblemDefinition> Problems = rcaCase.problems;
		render(rcaCase);
	}
	public static void newRCACase(String name) {
		models.RCACase rca = new models.RCACase(name);
		rca.save();
		//show(rca.id);
		index();
	}
	public static void create() {
		render();
	}

	public static void moveToNextStep(long id) {
		models.RCACase rcaCase = models.RCACase.findById(id);
		rcaCase.nextStep();
		show(rcaCase.id);
	}


}
