/*
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

package controllers;

import models.RCACase;
import models.User;
import models.enums.CompanySize;
import models.enums.RCACaseType;
import models.MandatoryFieldEmptyException;
import play.mvc.Controller;
import play.mvc.With;

import java.util.*;

import play.libs.F.*;
import models.events.*;
import play.Logger;
import com.google.gson.reflect.*;

/**
 * @author Mikko Valjus
 */
@With(Secure.class)
public class RCACaseController extends Controller {

	public static void createRCACase(String errorMessage) {
		String username = SecurityController.connected();
		RCACaseType[] types = RCACaseType.values();
		CompanySize[] companySizes = CompanySize.values();
		render(username, types, companySizes, errorMessage);
	}

	public static void create(String name, int type, boolean multinational, String companyName,
	                          int companySize, boolean isCasePublic) {
		String username = SecurityController.connected();
		User user = User.find("byEmail", username).first();
		RCACaseType rcaCaseType = RCACaseType.valueOf(type);
		CompanySize size = CompanySize.valueOf(companySize);
		RCACase rcaCase = null;
		try {
			rcaCase = user.addRCACase(name, rcaCaseType, multinational, companyName, size,
			                                  isCasePublic).save();
		} catch (MandatoryFieldEmptyException e) {
			createRCACase(e.getMessage());
		}
		show(rcaCase.id);
	}


	public static void show(Long id) {
    RCACase rcaCase = RCACase.findById(id);
  	String size = rcaCase.companySize.text;
  	String type = rcaCase.caseType.text;
    if (rcaCase != null) {
        render(rcaCase, type, size);
    } else {
        renderText("Unknown case id.");
    }
	}

	public static void getStream(Long id, Long timestamp) {

	}
	
	public static void waitMessages(Long id, Long lastReceived) {
	  RCACase rcaCase = RCACase.findById(id);
    List messages = await(rcaCase.nextMessages(lastReceived));
    renderJSON(messages, new TypeToken<List<IndexedEvent<Event>>>() {}.getType());
  }
  
  

}
