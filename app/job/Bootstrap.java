/*
 * Copyright (C) 2011 - 2013 by Eero Laukkanen, Risto Virtanen, Jussi Patana,
 * Juha Viljanen, Joona Koistinen, Pekka Rihtniemi, Mika Kekäle, Roope Hovi,
 * Mikko Valjus, Timo Lehtinen, Jaakko Harjuhahto, Jonne Viitanen, Jari Jaanto,
 * Toni Sevenius, Anssi Matti Helin, Jerome Saarinen, Markus Kere
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
import play.jobs.Job;
import play.jobs.OnApplicationStart;

import java.util.Calendar;
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
	public static final String TUTORIAL_USER_PASSWORD = "tutorial";


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
	        User tutorial = new User(TUTORIAL_USER_EMAIL, TUTORIAL_USER_PASSWORD);
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

            final Calendar calendar = Calendar.getInstance();
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
            calendar.set(2013,Calendar.JANUARY,8,13,2);
            adminsPrivateCase.created = calendar.getTime();
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
            calendar.set(2012,Calendar.OCTOBER,10,10,31);
            adminsOwnPrivateCase.created = calendar.getTime();
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
            calendar.set(2012,Calendar.MAY,8,9,40);
            adminsPublicCase.created = calendar.getTime();
            adminsPublicCase.save();
	        admin.addRCACase(adminsPublicCase);
	        admin.save();
	        tester.addRCACase(adminsPublicCase);
	        tester.save();

            RCACase test1 = new RCACase(admin);
            test1.caseName = "";
            test1.caseName = "the first date sorting test case";
            test1.caseTypeValue = 2;
            test1.caseGoals = "Test the program";
            test1.companySizeValue = 2;
            test1.description = "We are going to save the world with our ARCA-tool!";
            test1.isMultinational = true;
            test1.companyName = "WiRCA";
            test1.companyProducts = "ARCA-tool";
            test1.isCasePublic = true;
            test1.problem = new Cause(test1, test1.caseName, admin).save();
            calendar.set(2011,Calendar.DECEMBER,9,9,44);
            test1.created = calendar.getTime();
            test1.save();
            admin.addRCACase(test1);
            admin.save();

            RCACase test2 = new RCACase(admin);
            test2.caseName = "";
            test2.caseName = "test case number 2";
            test2.caseTypeValue = 2;
            test2.caseGoals = "Test the program";
            test2.companySizeValue = 2;
            test2.description = "We are going to save the world with our ARCA-tool!";
            test2.isMultinational = true;
            test2.companyName = "WiRCA";
            test2.companyProducts = "ARCA-tool";
            test2.isCasePublic = true;
            test2.problem = new Cause(test2, test2.caseName, admin).save();
            calendar.set(2011,Calendar.DECEMBER,9,9,45);
            test2.created = calendar.getTime();
            test2.save();
            admin.addRCACase(test2);
            admin.save();

            RCACase test3 = new RCACase(admin);
            test3.caseName = "";
            test3.caseName = "everybody should 3 test date sorting!";
            test3.caseTypeValue = 2;
            test3.caseGoals = "Test the program";
            test3.companySizeValue = 2;
            test3.description = "We are going to save the world with our ARCA-tool!";
            test3.isMultinational = true;
            test3.companyName = "WiRCA";
            test3.companyProducts = "ARCA-tool";
            test3.isCasePublic = true;
            test3.problem = new Cause(test3, test3.caseName, admin).save();
            calendar.set(2012,Calendar.FEBRUARY,28,9,4);
            test3.created = calendar.getTime();
            test3.save();
            admin.addRCACase(test3);
            admin.save();

            RCACase test4 = new RCACase(admin);
            test4.caseName = "";
            test4.caseName = "4 why to test date sorting?";
            test4.caseTypeValue = 2;
            test4.caseGoals = "Test the program";
            test4.companySizeValue = 2;
            test4.description = "We are going to save the world with our ARCA-tool!";
            test4.isMultinational = true;
            test4.companyName = "WiRCA";
            test4.companyProducts = "ARCA-tool";
            test4.isCasePublic = true;
            test4.problem = new Cause(test4, test4.caseName, admin).save();
            calendar.set(2011,Calendar.NOVEMBER,29,21,2);
            test4.created = calendar.getTime();
            test4.save();
            admin.addRCACase(test4);
            admin.save();

            RCACase test5 = new RCACase(admin);
            test5.caseName = "";
            test5.caseName = "TESTING TIMESTAMPS 5";
            test5.caseTypeValue = 2;
            test5.caseGoals = "Test the program";
            test5.companySizeValue = 2;
            test5.description = "We are going to save the world with our ARCA-tool!";
            test5.isMultinational = true;
            test5.companyName = "WiRCA";
            test5.companyProducts = "ARCA-tool";
            test5.isCasePublic = true;
            test5.problem = new Cause(test5, test5.caseName, admin).save();
            calendar.set(2012,Calendar.FEBRUARY,21,8,53);
            test5.created = calendar.getTime();
            test5.save();
            admin.addRCACase(test5);
            admin.save();

            RCACase test6 = new RCACase(admin);
            test6.caseName = "";
            test6.caseName = "case test 6";
            test6.caseTypeValue = 2;
            test6.caseGoals = "Test the program";
            test6.companySizeValue = 2;
            test6.description = "We are going to save the world with our ARCA-tool!";
            test6.isMultinational = true;
            test6.companyName = "WiRCA";
            test6.companyProducts = "ARCA-tool";
            test6.isCasePublic = true;
            test6.problem = new Cause(test6, test6.caseName, admin).save();
            calendar.set(2013,Calendar.FEBRUARY,26,12,24);
            test6.created = calendar.getTime();
            test6.save();
            admin.addRCACase(test6);
            admin.save();

            RCACase test7 = new RCACase(admin);
            test7.caseName = "";
            test7.caseName = "testing 7!";
            test7.caseTypeValue = 2;
            test7.caseGoals = "Test the program";
            test7.companySizeValue = 2;
            test7.description = "We are going to save the world with our ARCA-tool!";
            test7.isMultinational = true;
            test7.companyName = "WiRCA";
            test7.companyProducts = "ARCA-tool";
            test7.isCasePublic = true;
            test7.problem = new Cause(test7, test7.caseName, admin).save();
            calendar.set(2012,Calendar.AUGUST,31,11,58);
            test7.created = calendar.getTime();
            test7.save();
            admin.addRCACase(test7);
            admin.save();

            RCACase test8 = new RCACase(admin);
            test8.caseName = "";
            test8.caseName = "test 8";
            test8.caseTypeValue = 2;
            test8.caseGoals = "Test the program";
            test8.companySizeValue = 2;
            test8.description = "We are going to save the world with our ARCA-tool!";
            test8.isMultinational = true;
            test8.companyName = "WiRCA";
            test8.companyProducts = "ARCA-tool";
            test8.isCasePublic = true;
            test8.problem = new Cause(test8, test8.caseName, admin).save();
            calendar.set(2012,Calendar.AUGUST,31,11,58);
            test8.created = calendar.getTime();
            test8.save();
            admin.addRCACase(test8);
            admin.save();

	        Classification classification1 = new Classification(test2,"Management",admin,ClassificationDimension.WHERE_DIMENSION_ID,
	                                                            "MA", "MA");
	        Classification classification2 = new Classification(test2,"Software Testing",admin,ClassificationDimension.WHERE_DIMENSION_ID,
	                                                            "ST", "ST");
	        Classification classification3 = new Classification(test2,"Implementation Work",admin,ClassificationDimension.WHERE_DIMENSION_ID,
	                                                            "IM", "IM");
	        Classification classification4 = new Classification(test2,"Work Practices",admin,ClassificationDimension.WHAT_DIMENSION_ID,
	                                                            "WP", "WP");
	        Classification classification5 = new Classification(test2,"Methods",admin,ClassificationDimension.WHAT_DIMENSION_ID,
	                                                            "ME", "ME");
	        Classification classification6 = new Classification(test2,"Task Priority",admin,ClassificationDimension.WHAT_DIMENSION_ID,
	                                                            "TP", "TP");
	        Classification classification7 = new Classification(test2,"Monitoring",admin,ClassificationDimension.WHAT_DIMENSION_ID,
	                                                            "MO", "MO");
	        Classification classification8 = new Classification(test2,"Co-operation",admin,ClassificationDimension.WHAT_DIMENSION_ID,
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
