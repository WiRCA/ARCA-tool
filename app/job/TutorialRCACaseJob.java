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

import models.*;
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

		// Some classifications for "tutorialRCACase"
		Classification testClassification1 = new Classification(tutorialRCACase,"Management",user, ClassificationDimension.WHERE_DIMENSION_ID,
				                                         "Problems in Management", "MA");
		Classification testClassification2 = new Classification(tutorialRCACase,"Software Testing",user,ClassificationDimension.WHERE_DIMENSION_ID,
		                                                        "Problems in Testing", "ST");
		Classification testClassification3 = new Classification(tutorialRCACase,"Implementation Work",user,ClassificationDimension.WHERE_DIMENSION_ID,
		                                                        "Problems in Implementation", "IM");
		Classification testClassification4 = new Classification(tutorialRCACase,"Work Practices",user,ClassificationDimension.WHAT_DIMENSION_ID,
		                                                        "Problems in Work Practices", "WP");
		Classification testClassification5 = new Classification(tutorialRCACase,"Methods",user,ClassificationDimension.WHAT_DIMENSION_ID,
		                                                        "Problems in Methods", "ME");
		Classification testClassification6 = new Classification(tutorialRCACase,"Task Priority",user,ClassificationDimension.WHAT_DIMENSION_ID,
		                                                        "Problems in Task Prioritising", "TP");
		Classification testClassification7 = new Classification(tutorialRCACase,"Monitoring",user,ClassificationDimension.WHAT_DIMENSION_ID,
		                                                        "Problems in Monitoring", "MO");
		Classification testClassification8 = new Classification(tutorialRCACase,"Co-operation",user,ClassificationDimension.WHAT_DIMENSION_ID,
		                                                        "Problems in Co-operation", "CO");
		testClassification1.save();
		testClassification2.save();
		testClassification3.save();
		testClassification4.save();
		testClassification5.save();
		testClassification6.save();
		testClassification7.save();
		testClassification8.save();

        Cause activityNode = tutorialRCACase.problem
	            .addCause("The activity menu can be accessed by clicking on a node (like this one)", user);
		activityNode.xCoordinate = 0;
		activityNode.save();
        Cause relationshipTopNode = activityNode
		        .addCause("Clicking on the arrow creates a relationship arrow between two nodes. After clicking the " +
		                  "arrow click on the node to which you wish to make the relation from the initial node.", user);
		relationshipTopNode.yCoordinate = 160;
		relationshipTopNode.xCoordinate = 0;
		relationshipTopNode.save();
        Cause relationshipToNode = relationshipTopNode.addCause("Try adding a relation from this node to another node.", user);
		relationshipToNode.yCoordinate = 250;
		relationshipToNode.xCoordinate = 0;
		relationshipToNode.save();
        Cause relationshipFromNode = relationshipTopNode.addCause("This node has a relation to the node next to it.", user);
		relationshipFromNode.yCoordinate = 250;
		relationshipFromNode.xCoordinate = -200;
		relationshipFromNode.save();
		relationshipFromNode.addCause(relationshipToNode);
        Cause addCauseNode = activityNode.addCause("Clicking on the plus button creates a new node under the currently selected node",
                                     user);
		addCauseNode.yCoordinate = 160;
		addCauseNode.xCoordinate = -360;
		addCauseNode.save();
		Cause renameNode = addCauseNode.addCause("Cause can be renamed by clicking pencil button", user);
		renameNode.yCoordinate = 160;
		renameNode.xCoordinate = 120;
		renameNode.save();
        Cause removeNode = addCauseNode.addCause("Feel free to create a node! You can remove nodes you've created by " +
                                             "using the Remove button (trashcan).", user);
		removeNode.yCoordinate = 160;
		removeNode.xCoordinate = -180;
		removeNode.save();
        Cause correctiveIdeaTopNode = activityNode.addCause("Clicking on the light bulb allows to add corrective ideas to the node.",
                                             user);
		correctiveIdeaTopNode.yCoordinate = 160;
		correctiveIdeaTopNode.xCoordinate = 180;
		correctiveIdeaTopNode.save();
        Cause hasIdeasNode = correctiveIdeaTopNode.addCause("This node has ideas added to it.", user);
		hasIdeasNode.yCoordinate = 140;
		hasIdeasNode.xCoordinate = 130;
		hasIdeasNode.save();

		hasIdeasNode.addCorrection("Tutorial idea 1", "Description for the first idea");
		hasIdeasNode.addCorrection("Tutorial idea 2", "Description for the second idea");
		Logger.debug("Tutorial RCA case job ended for user %s", user);
	}
}
