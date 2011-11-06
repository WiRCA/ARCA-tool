package controllers;

import play.mvc.Controller;
import play.mvc.Before;

public class ApplicationController extends Controller {

    @Before
    public static void isConnected() {
        if (SecurityController.connected() != null) {
            UserController.index();
        }   
    }

	public static void index() {
		render();
	}


}