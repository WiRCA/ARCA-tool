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

package models.enums;

/**
 * @author Juha Viljanen
 */
public enum RCACaseType {

	SOFT(1, "RCACaseType.softwareProject"),
	HR(2, "RCACaseType.hr"),
	OTHER(3, "RCACaseType.other");

	/**
	* the value of the enum
	*/
	public Integer value;
	/**
	* the text of the enum
	*/
	public String text;

	/**
	* Basic constructor
	*/
	RCACaseType(Integer value, String text) {
		this.value = value;
		this.text = text;
	}

	/**
	* Get the RCACaseType vith value
	* @param value of the type
	* @return found RCACaseType or null
	*/
	public static RCACaseType valueOf(Integer id) {
		for (RCACaseType caseType : RCACaseType.values()) {
			if (caseType.value.equals(id)) {
				return caseType;
			}
		}
		return null;
	}
}
