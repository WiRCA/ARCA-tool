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

import job.Bootstrap;
import models.RCACase;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.mvc.Http;
import play.mvc.Router;
import play.test.Fixtures;
import play.test.FunctionalTest;

/**
 * @author Risto Virtanen
 */
public class RCACaseFunctionalTest extends FunctionalTest {

	@Before
	public void setUp() {
		Fixtures.deleteAllModels();
		new Bootstrap().doJob();


	}

	@Test
	public void testNullUserTest() {
		Http.Response response = GET(Router.reverse("RCACaseController.createRCACase").url);
		assertStatus(302, response);
		assertHeaderEquals("Location", "/login", response);
	}

	@Test
	public void nonexistentRCACaseTest() {
		Http.Request request = newRequest();
		request.url = Router.reverse("RCACaseController.show").url;
		request.method = "GET";

		request.params.put("id", "9999");
		Http.Response response = GET(request, request.url);
		assertStatus(404, response);
	}

	@Test
	public void getCSVForRCACase() {
		RCACase rcaCase = RCACase.find("caseName", "Test RCA case").first();
		assertNotNull(rcaCase);

		Http.Request request = newRequest();
		request.url = "/login";
		request.params.put("username", "admin@local");
		request.params.put("password", "admin");
		POST(request, request.url);

		request = newRequest();
		request.url = Router.reverse("RCACaseController.extractCSV").url;
		request.method = "GET";

		request.params.put("rcaCaseId", "9999");
		Http.Response response = GET(request, request.url);
		Logger.info(response.headers.toString());
		assertStatus(404, response);

		request.params.put("rcaCaseId", rcaCase.id.toString());
		response = GET(request, request.url);
		Logger.info(response.headers.toString());
		assertStatus(200, response);
	}
}
