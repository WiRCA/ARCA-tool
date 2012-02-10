package controllers;

/**
 * @author Risto Virtanen
 */

import play.mvc.Controller;
import play.mvc.With;

/**
 * Methods related to the contact page.
 */
@With(LanguageController.class)
public class ContactController extends Controller {

	/**
	 * Open index page.
	 */
	public static void index() {
		render();
	}

}
