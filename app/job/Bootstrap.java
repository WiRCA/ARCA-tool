/*
 * Copyright (C) 2011 by Eero Laukkanen, Risto Virtanen, Jussi Patana, Juha Viljanen,
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

package job;/*
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

import models.*;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Risto Virtanen
 */
@OnApplicationStart
public class Bootstrap extends Job {

	public static final String ADMIN_USER_EMAIL = "admin@local";
	public static final String ADMIN_USER_PASSWORD = "admin";
	public static final String TEST_USER_EMAIL = "tester@local";
	public static final String TEST_USER_PASSWORD = "tester";
	public static final String TUTORIAL_USER_EMAIL = "tutorial@local";
	public static final String TURORIAL_USER_PASSWORD = "tutorial";


    public void doJob() {
        // Check if the database is empty
        if(User.count() == 0) {
	        // Create classification dimensions
	        ClassificationDimension whatDimension = new
			        ClassificationDimension("What", ClassificationDimension.WHAT_DIMENSION_ID);
	        ClassificationDimension whereDimension = new
			        ClassificationDimension("Where", ClassificationDimension.WHERE_DIMENSION_ID);
	        whatDimension.save();
	        whereDimension.save();

	        // Admin user
	        User admin = new User(ADMIN_USER_EMAIL, ADMIN_USER_PASSWORD);
	        admin.name = "Admin user";
	        admin.save();

	        // Tester user
	        User tester = new User(TEST_USER_EMAIL, TEST_USER_PASSWORD);
		    tester.name = "Test user";
		    tester.save();

	        // Tutorial user
	        User tutorial = new User(TUTORIAL_USER_EMAIL, TURORIAL_USER_PASSWORD);
	        tutorial.name = "Tutorial user";
	        tutorial.save();

	        // First RCA case
	        RCACase firstRCACase = new RCACase(tester);
	        firstRCACase.caseName = "";
	        firstRCACase.caseName = "Test RCA case";
			firstRCACase.caseTypeValue = 2;
			firstRCACase.caseGoals = "Save the world";
			firstRCACase.companySizeValue = 2;
			firstRCACase.description = "We are going to save the world with our ARCA-tool!";
			firstRCACase.isMultinational = true;
			firstRCACase.companyName = "WiRCA";
			firstRCACase.companyProducts = "ARCA-tool";
			firstRCACase.isCasePublic = true;

	        // Problem of the first RCA case
	        firstRCACase.problem = new Cause(firstRCACase,  firstRCACase.caseName, tester).save();
	        firstRCACase.save();
	        tester.addRCACase(firstRCACase);
	        tester.save();
	        
	        Cause testNode1 = firstRCACase.problem.addCause("test node 1", tester);
			testNode1.xCoordinate = -100;
			testNode1.save();
	        Cause testNode2 = firstRCACase.problem.addCause("test node 2", tester);
	        Cause testNode3 = testNode1.addCause("test node 3", tester);
			testNode3.xCoordinate = -75;
			testNode3.save();
	        Cause testNode4 = testNode1.addCause("test node 4", tester);
	        Cause testNode5 = testNode1.addCause("test node 5", tester);
			testNode5.xCoordinate = -200;
			testNode5.save();
	        Cause testNode6 = testNode2.addCause("test node 6", tester);
			testNode6.xCoordinate = 0;
			testNode6.save();
	        Cause testNode7 = testNode5.addCause("test node 7", tester);
	        Cause testNode8 = testNode4.addCause("test node 8", tester);
			testNode8.xCoordinate = 0;
			testNode8.save();
	        Cause testNode9 = testNode5.addCause("test node 9", tester);
			testNode9.xCoordinate = -100;
			testNode9.save();
	        Cause testNode10 = testNode9.addCause("test node 10", tester);
	        testNode7.addCause(testNode3);
	        testNode8.addCause(testNode6);
	        testNode10.addCause(testNode6);

	        // Some classifications for "firstRCACase"
	        Classification testClassification1 = new Classification(firstRCACase,"Management",admin,ClassificationDimension.WHERE_DIMENSION_ID,
	                                                            "MA", "MA");
	        Classification testClassification2 = new Classification(firstRCACase,"Software Testing",admin,ClassificationDimension.WHERE_DIMENSION_ID,
	                                                            "ST", "ST");
	        Classification testClassification3 = new Classification(firstRCACase,"Implementation Work",admin,ClassificationDimension.WHERE_DIMENSION_ID,
	                                                            "IM", "IM");
	        Classification testClassification4 = new Classification(firstRCACase,"Work Practices",admin,ClassificationDimension.WHAT_DIMENSION_ID,
	                                                            "WP", "WP");
	        Classification testClassification5 = new Classification(firstRCACase,"Methods",admin,ClassificationDimension.WHAT_DIMENSION_ID,
	                                                            "ME", "ME");
	        Classification testClassification6 = new Classification(firstRCACase,"Task Priority",admin,ClassificationDimension.WHAT_DIMENSION_ID,
	                                                            "TP", "TP");
	        Classification testClassification7 = new Classification(firstRCACase,"Monitoring",admin,ClassificationDimension.WHAT_DIMENSION_ID,
	                                                            "MO", "MO");
	        Classification testClassification8 = new Classification(firstRCACase,"Co-operation",admin,ClassificationDimension.WHAT_DIMENSION_ID,
	                                                            "CO", "CO");
	        testClassification1.save();
	        testClassification2.save();
	        testClassification3.save();
	        testClassification4.save();
	        testClassification5.save();
	        testClassification6.save();
	        testClassification7.save();
	        testClassification8.save();

	        ClassificationPair pair = new ClassificationPair(testClassification1, testClassification4);
	        pair.save();
	        SortedSet<ClassificationPair> set1 = new TreeSet<ClassificationPair>();
	        set1.add(pair);
	        testNode1.setClassifications(set1);
	        testNode1.save();

	        ClassificationPair pair2 = new ClassificationPair(testClassification1, testClassification4);
	        pair2.save();
	        SortedSet<ClassificationPair> set2 = new TreeSet<ClassificationPair>();
	        set2.add(pair2);
	        testNode2.setClassifications(set2);
	        testNode2.save();


	        RCACase adminsPrivateCase = new RCACase(admin);
	        adminsPrivateCase.caseName = "";
	        adminsPrivateCase.caseName = "Admin's private RCA case";
	        adminsPrivateCase.caseTypeValue = 2;
			adminsPrivateCase.caseGoals = "Test the program";
			adminsPrivateCase.companySizeValue = 2;
			adminsPrivateCase.description = "We are going to save the world with our ARCA-tool!";
			adminsPrivateCase.isMultinational = true;
			adminsPrivateCase.companyName = "WiRCA";
			adminsPrivateCase.companyProducts = "ARCA-tool";
	        adminsPrivateCase.isCasePublic = false;
	        adminsPrivateCase.problem = new Cause(adminsPrivateCase, adminsPrivateCase.caseName, admin).save();
	        adminsPrivateCase.save();
	        admin.addRCACase(adminsPrivateCase);
	        admin.save();
	        tester.addRCACase(adminsPrivateCase);
	        tester.save();

	        RCACase adminsOwnPrivateCase = new RCACase(admin);
            adminsOwnPrivateCase.caseName = "";
            adminsOwnPrivateCase.caseName = "Admin's own private RCA case";
            adminsOwnPrivateCase.caseTypeValue = 2;
            adminsOwnPrivateCase.caseGoals = "Test the program";
            adminsOwnPrivateCase.companySizeValue = 2;
            adminsOwnPrivateCase.description = "We are going to save the world with our ARCA-tool!";
            adminsOwnPrivateCase.isMultinational = true;
            adminsOwnPrivateCase.companyName = "WiRCA";
	        adminsOwnPrivateCase.companyProducts = "ARCA-tool";
            adminsOwnPrivateCase.isCasePublic = false;
            adminsOwnPrivateCase.problem = new Cause(adminsOwnPrivateCase, adminsOwnPrivateCase.caseName, admin).save();
            adminsOwnPrivateCase.save();
            admin.addRCACase(adminsOwnPrivateCase);
            admin.save();

	        RCACase adminsPublicCase = new RCACase(admin);
	        adminsPublicCase.caseName = "";
	        adminsPublicCase.caseName = "Admin's public RCA case";
	        adminsPublicCase.caseTypeValue = 2;
			adminsPublicCase.caseGoals = "Test the program";
			adminsPublicCase.companySizeValue = 2;
			adminsPublicCase.description = "We are going to save the world with our ARCA-tool!";
			adminsPublicCase.isMultinational = true;
			adminsPublicCase.companyName = "WiRCA";
			adminsPublicCase.companyProducts = "ARCA-tool";
	        adminsPublicCase.isCasePublic = true;
	        adminsPublicCase.problem = new Cause(adminsPublicCase, adminsPublicCase.caseName, admin).save();
	        adminsPublicCase.save();
	        admin.addRCACase(adminsPublicCase);
	        admin.save();
	        tester.addRCACase(adminsPublicCase);
	        tester.save();


	        Classification classification1 = new Classification(adminsPublicCase,"Management",admin,ClassificationDimension.WHERE_DIMENSION_ID,
	                                                            "MA", "MA");
	        Classification classification2 = new Classification(adminsPublicCase,"Software Testing",admin,ClassificationDimension.WHERE_DIMENSION_ID,
	                                                            "ST", "ST");
	        Classification classification3 = new Classification(adminsPublicCase,"Implementation Work",admin,ClassificationDimension.WHERE_DIMENSION_ID,
	                                                            "IM", "IM");
	        Classification classification4 = new Classification(adminsPublicCase,"Work Practices",admin,ClassificationDimension.WHAT_DIMENSION_ID,
	                                                            "WP", "WP");
	        Classification classification5 = new Classification(adminsPublicCase,"Methods",admin,ClassificationDimension.WHAT_DIMENSION_ID,
	                                                            "ME", "ME");
	        Classification classification6 = new Classification(adminsPublicCase,"Task Priority",admin,ClassificationDimension.WHAT_DIMENSION_ID,
	                                                            "TP", "TP");
	        Classification classification7 = new Classification(adminsPublicCase,"Monitoring",admin,ClassificationDimension.WHAT_DIMENSION_ID,
	                                                            "MO", "MO");
	        Classification classification8 = new Classification(adminsPublicCase,"Co-operation",admin,ClassificationDimension.WHAT_DIMENSION_ID,
	                                                            "CO", "CO");
	        classification1.save();
	        classification2.save();
	        classification3.save();
	        classification4.save();
	        classification5.save();
	        classification6.save();
	        classification7.save();
	        classification8.save();

	        //new TutorialRCACaseJob().doJob(tester, true);
	        new TutorialRCACaseJob().doJob(tutorial,true);

	    }
    }
}
