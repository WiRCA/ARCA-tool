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

import models.Cause;
import models.RCACase;
import models.User;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

/**
 * @author Risto Virtanen
 */
@OnApplicationStart
public class Bootstrap extends Job {

	public static final String ADMIN_USER_EMAIL = "admin@local";
	public static final String ADMIN_USER_PASSWORD = "admin";
	public static final String TEST_USER_EMAIL = "tester@local";
	public static final String TEST_USER_PASSWORD = "tester";

    public void doJob() {
        // Check if the database is empty
        if(User.count() == 0) {
	        // Admin user
	        User admin = new User(ADMIN_USER_EMAIL, ADMIN_USER_PASSWORD);
	        admin.name = "Admin user";
	        admin.save();

	        // Tester user
	        User tester = new User(TEST_USER_EMAIL, TEST_USER_PASSWORD);
		    tester.name = "Test user";
		    tester.save();

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
			firstRCACase.isCasePublic = false;

	        // Problem of the first RCA case
	        firstRCACase.problem = new Cause(firstRCACase,  firstRCACase.caseName, tester).save();
	        firstRCACase.save();
	        tester.addRCACase(firstRCACase);
	        tester.save();
	    }
    }
}
