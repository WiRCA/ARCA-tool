/*
 * Copyright (C) 2012 by Eero Laukkanen, Risto Virtanen, Jussi Patana, Juha Viljanen,
 * Joona Koistinen, Pekka Rihtniemi, Mika Kekäle, Roope Hovi, Mikko Valjus,
 * Timo Lehtinen, Jaakko Harjuhahto
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

package job;

import models.Cause;
import models.RCACase;
import models.User;
import models.enums.CompanySize;
import models.enums.RCACaseType;
import play.Logger;
import play.jobs.Job;

/**
 * @author Risto Virtanen
 */
public class TutorialRCACaseJob extends Job {

	/**
	 * Generate a tutorial rca case for a user
	 * @param user the user whom to generate a tutorial case
	 */
	public void doJob(User user) {
		Logger.debug("Tutorial RCA case job started for user %s", user);
		// Tutorial RCA case
        RCACase tutorialRCACase = new RCACase(user);
        tutorialRCACase.caseName = "Tutorial RCA case";
		tutorialRCACase.caseTypeValue = RCACaseType.OTHER.value;
		tutorialRCACase.caseGoals = "To teach the use of the tool.";
		tutorialRCACase.companySizeValue = CompanySize.TEN.value;
		tutorialRCACase.description = "A tutorial case to show different functionalities in the tool and to help get started.";
		tutorialRCACase.isMultinational = false;
		tutorialRCACase.companyName = "WiRCA";
		tutorialRCACase.companyProducts = "";
		tutorialRCACase.isCasePublic = false;

        // Problem of the tutorial RCA case
        tutorialRCACase.problem = new Cause(tutorialRCACase,  tutorialRCACase.caseName, user).save();
        tutorialRCACase.save();
		user.addRCACase(tutorialRCACase);
        user.save();

        Cause node1 = tutorialRCACase.problem
	            .addCause("The activity menu can be accessed by clicking on a node (like this one)", user);
		node1.xCoordinate = 0;
		node1.save();
        Cause node2 = node1
		        .addCause("Clicking on the arrow creates a relationship arrow between two nodes. After clicking the " +
		                  "arrow click on the node to which you wish to make the relation from the initial node.", user);
		node2.yCoordinate = 160;
		node2.xCoordinate = 0;
		node2.save();
        Cause node3 = node2.addCause("Try adding a relation from this node to another node.", user);
		node3.yCoordinate = 250;
		node3.xCoordinate = 0;
		node3.save();
        Cause node4 = node2.addCause("This node has a relation to the node next to it.", user);
		node4.yCoordinate = 250;
		node4.xCoordinate = -200;
		node4.save();
		node4.addCause(node3);
        Cause node5 = node1.addCause("Clicking on the plus button creates a new node under the currently selected node",
                                     user);
		node5.yCoordinate = 160;
		node5.xCoordinate = -160;
		node5.save();
        Cause node6 = node5.addCause("Feel free to create a node! You can remove nodes you've created by " +
                                             "using the Remove button in the upper right corner of the menu.", user);
		node6.yCoordinate = 85;
		node6.xCoordinate = -220;
		node6.save();
        Cause node7 = node1.addCause("Clicking on the light bulb allows to add corrective ideas to the node.",
                                             user);
		node7.yCoordinate = 160;
		node7.xCoordinate = 180;
		node7.save();
        Cause node8 = node7.addCause("This node has ideas added to it.", user);
		node8.yCoordinate = 140;
		node8.xCoordinate = 130;
		node8.save();

		node8.addCorrection("Tutorial idea 1", "Description for the first idea");
		node8.addCorrection("Tutorial idea 2", "Description for the second idea");
		Logger.debug("Tutorial RCA case job ended for user %s", user);
	}
}
