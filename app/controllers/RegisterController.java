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

package controllers;

import models.Invitation;
import models.User;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.libs.OpenID;
import play.mvc.Controller;
import play.mvc.With;

/**
 * @author Juha Viljanen
 */
@With(LanguageController.class)
public class RegisterController extends Controller {
	public static void registerUser() {
		render();
	}

	public static void register(@Valid User user, @Required String password2) {

		validation.isTrue(User.find("byEmail", user.email).first() == null &&
		                  Invitation.find("byEmail", user.email).first() == null )
		          .key("user.email").message("register.emailExists");
		validation.equals(user.password, password2).key("user.password").message("register.passwordsDidNotMatch");

		if (validation.hasErrors()) {
			params.flash(); // add http parameters to the flash scope
			validation.keep(); // keep the errors for the next request
			registerUser();
		}

		user.changePassword(password2);
		user.save();

        // Mark user as connected
        session.put("username", user.email);
		ApplicationController.index();
	}

	public static void googleLogin() {
		if (OpenID.isAuthenticationResponse()) {
			OpenID.UserInfo verifiedUser = OpenID.getVerifiedID();
			if (verifiedUser == null) {
				flash.error("Oops. Authentication has failed");
				ApplicationController.index();
			}
			String openid = verifiedUser.id;
			String email = verifiedUser.extensions.get("email");
			String firstName = verifiedUser.extensions.get("firstname");
			String lastName = verifiedUser.extensions.get("lastname");
			session.put("user", verifiedUser.id);
			ApplicationController.index();
		} else {
			if (!OpenID.id("https://www.google.com/accounts/o8/id") // will redirect the user
					.required("email", "http://axschema.org/contact/email")
					.required("firstname", "http://axschema.org/namePerson/first")
					.required("lastname", "http://axschema.org/namePerson/last")
					.verify()) {
				flash.error("Cannot verify your OpenID");
				ApplicationController.index();
			}
		}
	}
}
