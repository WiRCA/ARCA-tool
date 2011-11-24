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

package notifiers;

import models.Invitation;
import models.RCACase;
import models.User;
import play.Logger;
import play.Play;
import play.mvc.Mailer;
import utils.EncodingUtils;

/**
 * E-mail sender
 * Documentation of play.mvc.Mailer can be found from http://www.playframework.org/documentation/1.2.3/emails
 * @author Risto Virtanen
 */
public class Mails extends Mailer {

	private final static String DEFAULT_SENDER = Play.configuration.getProperty("mail.from.name") +
		        " <" + Play.configuration.getProperty("mail.from" +".address") + ">";

	/**
	 * This e-mail is sent when a new user is invited to a RCA case
	 * @param user User that invited new user
	 * @param invitedUser User that is invited
	 * @param rcaCase RCA case which the user is invited to
	 */
	public static void invite(User user, Invitation invitedUser, RCACase rcaCase) {
		setFrom(DEFAULT_SENDER);
		setSubject("%s invited You to Root Cause Analysis session %s", user.name, rcaCase.caseName);
		addRecipient(invitedUser.email);
		String inviteHash = EncodingUtils.encodeSHA1Base64(invitedUser.hash);
		Logger.info("User %s sent an invitation mail to %s to share %s", user, invitedUser, rcaCase);
		send(user, invitedUser, inviteHash, rcaCase);
	}

}
