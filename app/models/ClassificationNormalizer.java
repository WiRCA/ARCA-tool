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

package models;

import java.util.*;

/**
 * A helper class for merging multiple cases together for dimension diagrams or classification tables
 */
public class ClassificationNormalizer {
	/**
	 * A map of classification name => Classification instance
	 */
	private Map<String, Classification> nameToClassification;


	/**
	 * Constructor
	 */
	public ClassificationNormalizer() {
		this.nameToClassification = new HashMap<String, Classification>();
	}


	/**
	 * Normalizes a classification, which is a simple process:
	 * 1) If the classification's name is found in the nameToClassification map,
	 *    return the Classification instance corresponding to the name
	 * 2) Otherwise add this name=>Classification pair to the map and return
	 *    this classification
	 * @return normalized classification
	 */
	public Classification normalizeClassification(Classification c) {
		if (this.nameToClassification.containsKey(c.name)) {
			return this.nameToClassification.get(c.name);
		} else {
			this.nameToClassification.put(c.name, c);
			return c;
		}
	}


	/**
	 * A convenience function for normalizing ClassificationPairs
	 * @param pair the ClassificationPair to normalize
	 * @return normalized ClassificationPair
	 */
	public ClassificationPair normalizePair(ClassificationPair pair) {
		return new ClassificationPair(
				this.normalizeClassification(pair.parent),
				this.normalizeClassification(pair.child)
		);
	}


	/**
	 * Returns the amount of normalized classifications.
	 */
	public int size() {
		HashSet<Classification> uniqueEntries = new HashSet<Classification>();
		for (Map.Entry<String, Classification> entry : this.nameToClassification.entrySet()) {
			uniqueEntries.add(entry.getValue());
		}
		return uniqueEntries.size();
	}


	/**
	 * Returns all normalized "what" classifications.
	 */
	public List<Classification> getWhatClassifications() {
		List<Classification> out = new ArrayList<Classification>();
		for (Map.Entry<String, Classification> entry : this.nameToClassification.entrySet()) {
			if (entry.getValue().classificationDimension == ClassificationDimension.WHAT_DIMENSION_ID) {
				out.add(entry.getValue());
			}
		}
		return out;
	}
}