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

package models;

import utils.IdComparableModel;

import javax.persistence.*;
import java.util.List;

@PersistenceUnit(name = "maindb")
@Entity(name = "dimension")
public class ClassificationDimension extends IdComparableModel {

	/**
	 * Consts for dimension IDs
	 */
	public static final int FIRST_DIMENSION_ID = 1;
	public static final int SECOND_DIMENSION_ID = 2;

	/**
	 * the name of the dimension
	 */
	public String name;

	/**
	 * the id number of the dimension
	 */
	public Integer dimensionId;

	/**
	 * Basic constructor
	 */
	public ClassificationDimension(String name, Integer dimensionId) {
		this.name = name;
		this.dimensionId = dimensionId;
	}

	/**
	 *  Returns the corresponding classification dimension for given id number
	 */
	public static ClassificationDimension valueOf(Integer classificationDimensionId) {
		List<ClassificationDimension> dimensionList = ClassificationDimension.find(
				"SELECT c FROM dimension AS c WHERE dimensionId=?",
				classificationDimensionId
		).fetch();
		if (dimensionList != null) {
			return dimensionList.get(0);
		} else {
			return null;
		}
	}
}