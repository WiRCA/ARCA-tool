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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import play.Logger;

import java.util.HashMap;
import java.util.SortedSet;

public class ClassificationRelationMap {
	/**
	 * Simple relations from a WHERE classification to another
	 */
	private HashMap<Classification, HashMap<Classification, ClassificationRelation>> simpleRelations;

	/**
	 * Relations from a WHERE-WHAT classification pair to another
	 */
	private HashMap<ClassificationPair, HashMap<ClassificationPair, ClassificationRelation>> pairRelations;


	/**
	 * Constructor, creates an empty map
	 */
	public ClassificationRelationMap() {
		this.simpleRelations = new HashMap<Classification, HashMap<Classification, ClassificationRelation>>();
		this.pairRelations = new HashMap<ClassificationPair, HashMap<ClassificationPair, ClassificationRelation>>();
	}


	/**
	 * Adds the relation to the map, adds simple and pair relations. Automatically reorders first and second so they
	 * are in the canonized order (ie. ascending by pair ID)
	 * @param first the first pair classification pair
	 * @param second the second classification pair
	 * @param relation the relation of the pairs
	 */
	public void addRelation(ClassificationPair first, ClassificationPair second, ClassificationRelation relation) {
		// Check that we're not given equal classification pairs (a pair cannot have a relation with itself)
		if (first.equals(second)) {
			//throw new IllegalArgumentException("The two pairs should not be equal");
		}

		// Order the pairs correctly - relations are always two-way, so we canonize the relation by having
		// the one with the lower ID as the "first" one, technically the HashMap's key
		if (first.compareTo(second) > 0) {
			ClassificationPair temp = first;
			first = second;
			second = temp;
		}

		// Add the simple relation's first key if it does not exist
		if (!this.simpleRelations.containsKey(first.parent)) {
			this.simpleRelations.put(first.parent, new HashMap<Classification, ClassificationRelation>());
		}

		// Create the simple relation with zero relation strength and likes if it does not exist
		if (!this.simpleRelations.get(first.parent).containsKey(second.parent)) {
			this.simpleRelations.get(first.parent).put(second.parent, new ClassificationRelation(0, 0));
		}

		// Add the simple relation
		this.simpleRelations.get(first.parent).get(second.parent).add(relation);

		// Add the pair relation's first key if it does not exist
		if (!this.pairRelations.containsKey(first)) {
			this.pairRelations.put(first, new HashMap<ClassificationPair, ClassificationRelation>());
		}

		// Create the pair relation with zero strength and likes if it does not exist
		if (!this.pairRelations.get(first).containsKey(second)) {
			this.pairRelations.get(first).put(second, new ClassificationRelation(0, 0));
		}

		// Add the pair relation
		this.pairRelations.get(first).get(second).add(relation);
	}


	/**
	 * Creates a relation map for a complete RCA case
	 * @param rcaCase the RCA case
	 * @return ClassificationRelationMap
	 */
	public static ClassificationRelationMap fromCase(RCACase rcaCase) {
		ClassificationRelationMap map = new ClassificationRelationMap();

		// Temporary variables
		Cause causeTo;
		ClassificationRelation relationData;

		// Loop through the causes and their relations, only considering "to" relations so we don't get duplicates
		// (This is a slight performance loss as all causes have both ways of relations known, but it is most
		// probably negligible.)
		SortedSet<Cause> causes = rcaCase.causes;
		for (Cause causeFrom : causes) {
			for (Relation relation : causeFrom.causeRelations) {
				causeTo = relation.causeTo;

				// Add relations to all of the combinations between the classification pairs of the related
				// causes. Funky.
				for (ClassificationPair fromPair : causeFrom.classifications) {
					for (ClassificationPair toPair : causeTo.classifications) {
						// The relation strength for these two is one, as we're only handling one pair of
						// classification pairs here, and the amount of likes is the amount of likes for the two
						// causes combined.
						relationData = new ClassificationRelation(1, causeFrom.likes.size() + causeTo.likes.size());
						map.addRelation(fromPair, toPair, relationData);
					}
				}
			}
		}

		return map;
	}


	/**
	 * A relationship between a classification or a classification pair
	 */
	public static class ClassificationRelation {
		/**
		 * The amount of relations between the two classifications
		 */
		public int strength;

		/**
		 * The amount of likes of the causes classified by the classifications of the relation
		 */
		public int likes;


		/**
		 * Constructor of the class
		 * @param strength the strength of the relation (ie. the amount of relations)
		 * @param likes the amount of likes of the causes with the relevant classifications
		 */
		public ClassificationRelation(int strength, int likes) {
			this.strength = strength;
			this.likes = likes;
		}


		/**
		 * Adds the strength and likes of the other relation to this one
		 * @param other another ClassificationRelation
		 */
		public void add(ClassificationRelation other) {
			this.strength += other.strength;
			this.likes += other.likes;
		}
	}


	/**
	 * Returns the data of the map in JSON format
	 * @return JSON data
	 */
	public String toJson() {
		JsonObject out = new JsonObject();

		JsonObject child, grandChild;
		ClassificationRelation relation;

		// Construct the simple relation map
		JsonObject simpleRelations = new JsonObject();
		for (Classification key : this.simpleRelations.keySet()) {
			child = new JsonObject();
			for (Classification subKey : this.simpleRelations.get(key).keySet()) {
				relation = this.simpleRelations.get(key).get(subKey);
				grandChild = new JsonObject();
				grandChild.addProperty("strength", relation.strength);
				grandChild.addProperty("likes", relation.likes);
				child.add(subKey.id.toString(), grandChild);
			}
			simpleRelations.add(key.id.toString(), child);
		}

		// Construct the pair relation map
		JsonObject pairRelations = new JsonObject();
		for (ClassificationPair key : this.pairRelations.keySet()) {
			child = new JsonObject();
			for (ClassificationPair subKey : this.pairRelations.get(key).keySet()) {
				relation = this.pairRelations.get(key).get(subKey);
				grandChild = new JsonObject();
				grandChild.addProperty("strength", relation.strength);
				grandChild.addProperty("likes", relation.likes);
				child.add(subKey.parent.id + ":" + subKey.child.id, grandChild);
			}
			pairRelations.add(key.parent.id + ":" + key.child.id, child);
		}

		// Return
		out.add("simpleRelations", simpleRelations);
		out.add("pairRelations", pairRelations);
		return out.toString();
	}
}