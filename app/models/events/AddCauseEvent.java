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

package models.events;

import models.Cause;

/**
* Add sub cause to cause event
*/
public class AddCauseEvent extends Event {

	/**
	* the name of the new cause
	*/
	public final String text;

	/**
	* the id of the old cause
	*/
	public final String causeFrom;

	/**
	* the id of te new sub cause
	*/
	public final String causeTo;

	/**
	* id of the creator user of the sub cause
	*/
	public final String creatorId;

	/**
	 * X coordinate of the sub cause
	 */
	public final String x;

	/**
	 * Y coordinate of the sub cause
	 */
	public final String y;

	/**
	 * Will we classify the cause at creation
	 */
	public final boolean classify;

	/**
	 * Basic constructor
	 */
	public AddCauseEvent(Cause cause, Long causeFrom, int x, int y, boolean classify) {
		super("addcauseevent");
		this.causeTo = Long.toString(cause.id);
		this.text = cause.name.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;");
		this.causeFrom = String.valueOf(causeFrom);
		this.creatorId = cause.creatorId == null ? null : Long.toString(cause.creatorId);
		this.x = String.valueOf(x);
		this.y = String.valueOf(y);
		this.classify = classify;
	}
}



