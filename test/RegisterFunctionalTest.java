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

import models.Invitation;
import models.RCACase;
import models.User;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Router;
import play.mvc.Scope;
import play.test.Fixtures;
import play.test.FunctionalTest;
import utils.EncodingUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Risto Virtanen
 */
public class RegisterFunctionalTest extends FunctionalTest {

	@Test
	public void registerInvitationTest() {
		Fixtures.deleteAllModels();
		Fixtures.loadModels("data.yml");
		User admin = User.find("byEmail", "admin@local").first();

		Map<String, String> params = new HashMap<String, String>();
		params.put("username", admin.email);
		params.put("password", "admin");
		Http.Response response = POST("/login", params);
		assertStatus(302, response);

		String registerInvitationUrl = Router.reverse("RegisterController.registerInvitation").url;
		Http.Request request = newRequest();
		request.cookies = response.cookies;
		request.url = registerInvitationUrl;
		request.method = "GET";
		RCACase rcaCase = new RCACase("test", 1, "test", "test", false, "test", 1, "test", false, admin).save();
		Invitation invitation = new Invitation("inv@local.fi");
		invitation.caseIds.add(rcaCase.id);
		invitation.save();
		String inviteHash = EncodingUtils.encodeSHA1Base64(invitation.hash);
		//Map<String, Object> map = new HashMap<String, Object>();
        response = GET(request, request.url);
		assertStatus(302, response);

		response = GET("/logout");
		assertStatus(302, response);
		request.cookies = response.cookies;
		request.path = request.url;

		request.params.put("invitationId", invitation.id.toString());
        response = makeRequest(request);
		assertStatus(404, response);
		request.params.put("rcaCaseId", rcaCase.id.toString());
        response = makeRequest(request);
		assertStatus(404, response);
		request.params.put("inviteHash", "INVALIDHASH");
        response = makeRequest(request);
		assertStatus(403, response);
		request.params.put("inviteHash", inviteHash);
        response = makeRequest(request);
		assertStatus(200, response);
		params = new HashMap<String, String>();
		params.put("user.email", "notEqual");
		params.put("password2", "");
		params.put("invitationId", invitation.id.toString());
		params.put("rcaCaseId", rcaCase.id.toString());
		response = POST("/register", params);
		assertIsOk(response);
		params.put("user.email", "notEqual");
		response = POST("/register", params);
		assertIsOk(response);
		params.put("user.email", invitation.email);
		response = POST("/register", params);
		assertIsOk(response);
		params.put("user.password", "same");
		response = POST("/register", params);
		assertIsOk(response);
		params.put("password2", "same");
		response = POST("/register", params);
		assertIsOk(response);
		params.put("user.name", "test");
		response = POST("/register", params);
		assertStatus(302, response);
		response = POST("/register", params);
		assertIsNotFound(response);

		response = GET("/logout");
		assertStatus(302, response);
		Scope.Session.current().remove("username");


		Invitation secondInvitation = new Invitation("second@te.st").save();
		params = new HashMap<String, String>();
		params.put("user.name", "test");
		params.put("user.email", secondInvitation.email);
		params.put("user.password", "same");
		params.put("password2", "same");
		params.put("invitationId", secondInvitation.id.toString());
		params.put("rcaCaseId", rcaCase.id.toString());
		response = POST("/register", params);
		assertStatus(302, response);
		response = POST("/register", params);
		assertIsNotFound(response);

		response = GET("/logout");
		assertStatus(302, response);
		Scope.Session.current().remove("username");
		response = GET("/");
		assertStatus(302, response);
	}

	@Test
	public void registerInvitedUser() {
		Fixtures.deleteAllModels();
		new Invitation("invitation@te.st").save();
		Map<String, String> params = new HashMap<String, String>();
		params.put("user.name", "test");
		params.put("user.email", "invitation@te.st");
		params.put("user.password", "same");
		params.put("password2", "same");
		Http.Response response = POST("/register", params);
		assertStatus(302, response);

		response = GET("/logout");
		assertStatus(302, response);
		Scope.Session.current().remove("username");
		response = GET("/");
		assertIsOk(response);
	}

	@Test
	public void tryToRegisterExistingUser() {
		Fixtures.deleteAllModels();
		Fixtures.loadModels("data.yml");
		Map<String, String> params = new HashMap<String, String>();
		params.put("user.name", "test");
		params.put("user.email", "admin@local");
		params.put("user.password", "same");
		params.put("password2", "same");
		Http.Response response = POST("/register", params);
		assertStatus(302, response);
	}
}
