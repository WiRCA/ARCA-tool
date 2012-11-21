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

package models.enums;

/**
 * Type for dimensions.
 */
public enum DimensionType {

	WHAT(1, "What"),
	WHERE(2, "Where");

	/**
	 * the value of the enum
	 */
	public Integer dimensionId;

	/**
	 * the text of the enum
	 */
	public String text;

	/**
	 * Basic constructor
	 */
	DimensionType(Integer dimensionId, String text) {
		this.dimensionId = dimensionId;
		this.text = text;
	}

	public int getId() {
		return dimensionId;
	}

	/**
	 * Get the DimensionType with the given ID
	 * @param id of the DimensionType
	 * @return found DimensionType or null
	 */
	public static DimensionType valueOf(int id) {
		for (DimensionType dim : DimensionType.values()) {
			if (dim.dimensionId.equals(id)) {
				return dim;
			}
		}
		return null;
	}
}