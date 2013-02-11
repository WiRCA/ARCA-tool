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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import play.Logger;

import java.util.*;

public class ClassificationRelationMap {
	/**
	 * Simple relations from a WHERE classification to another
	 */
	private Map<Classification, Map<Classification, ClassificationRelation>> simpleRelations;

	/**
	 * Relations from a WHERE-WHAT classification pair to another
	 */
	private Map<ClassificationPair, Map<ClassificationPair, ClassificationRelation>> pairRelations;

	/**
	 * WHERE classifications connected to the root
	 */
	private Map<Classification, ClassificationRelation> rootRelations;

	/**
	 * A ClassificationNormalizer instance used for loading multiple cases.
	 */
	private ClassificationNormalizer normalizer;


	/**
	 * Constructor, creates an empty map
	 */
	public ClassificationRelationMap() {
		this.simpleRelations = new HashMap<Classification, Map<Classification, ClassificationRelation>>();
		this.pairRelations = new HashMap<ClassificationPair, Map<ClassificationPair, ClassificationRelation>>();
		this.rootRelations = new HashMap<Classification, ClassificationRelation>();
		this.normalizer = new ClassificationNormalizer();
	}


	/**
	 * Adds the relation to the map, adds simple and pair relations. Automatically reorders first and second so they
	 * are in the canonized order (ie. ascending by pair ID)
	 * @param first the first pair classification pair
	 * @param second the second classification pair
	 * @param relation the relation of the pairs
	 */
	public void addRelation(ClassificationPair first, ClassificationPair second, ClassificationRelation relation) {
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
			this.simpleRelations.get(first.parent).put(second.parent, new ClassificationRelation(0, 0, 0));
		}

		// Add the simple relation
		this.simpleRelations.get(first.parent).get(second.parent).add(relation);

		// Add the pair relation's first key if it does not exist
		if (!this.pairRelations.containsKey(first)) {
			this.pairRelations.put(first, new HashMap<ClassificationPair, ClassificationRelation>());
		}

		// Create the pair relation with zero strength and likes if it does not exist
		if (!this.pairRelations.get(first).containsKey(second)) {
			this.pairRelations.get(first).put(second, new ClassificationRelation(0, 0, 0));
		}

		// Add the pair relation
		this.pairRelations.get(first).get(second).add(relation);
	}


	/**
	 * Adds a new root relation to the map or updates an existing one if present.
	 * @param classification a (WHERE) classification connected to the root
	 * @param relationData the strength and likes of a relation
	 */
	public void addRootRelation(Classification classification, ClassificationRelation relationData) {
		classification = this.normalizer.normalizeClassification(classification);
		if (this.rootRelations.containsKey(classification)) {
			this.rootRelations.get(classification).add(relationData);
		} else {
			this.rootRelations.put(classification, relationData);
		}
	}


	/**
	 * Returns the relevance (ie. relation counts and likes) of each classification in the relation map.
	 * @return a map in the form [Classification ID -> relevance]
	 */
	public HashMap<Long, Integer> getClassificationRelevances() {
		HashMap<Long, Integer> out = new HashMap<Long, Integer>();
		RCACase rcaCase = this.rootRelations.keySet().iterator().next().rcaCase;

		// Loop through all classifications
		for (Classification classification : rcaCase.getClassifications()) {
			SortedSet<Cause> causes = rcaCase.causes;
			// Loop causes in current case
			for (Cause cause : causes) {
				// Loop classifications in cause
				for (ClassificationPair classificationPair : cause.classifications) {
					// If the cause has the classification, add relevance info
					if (classificationPair.parent == classification) {
						if (out.containsKey(classification.id)) {
							out.put(classification.id, out.get(classification.id) + cause.effectRelations.size() + cause.causeRelations.size() + cause.likes.size());
						} else {
							out.put(classification.id, cause.effectRelations.size() + cause.causeRelations.size() + cause.likes.size());
						}
					}
				}
			}
		}
		return out;
	}


	/**
	 * Loads data from a RCA case into the relation map. All classifications are normalized, so multiple cases can
	 * be loaded to the map with ease and classifications with the same name are automatically merged into one.
	 * @param rcaCase the RCA case to use
	 */
	public void loadCase(RCACase rcaCase) {
		// Temporary variables
		Cause causeFrom;
		ClassificationRelation relationData;

		// Add root relations for the root node's adjacencies
		// As in some edge cases we might have nodes with bidirectional adjacencies
		// to the root node, add the causes that are processed here to a set so we
		// can skip them later to avoid erroneously double-strength edges.
		TreeSet<Cause> addedRootCauses = new TreeSet<Cause>();
		for (Relation relation : rcaCase.problem.causeRelations) {
			addedRootCauses.add(relation.causeFrom);
			for (ClassificationPair fromPair : relation.causeFrom.classifications) {
				relationData = new ClassificationRelation(
					1, rcaCase.problem.likes.size() + relation.causeFrom.likes.size(),
					rcaCase.problem.corrections.size() + relation.causeFrom.corrections.size()
				);
				this.addRootRelation(this.normalizer.normalizeClassification(fromPair.parent), relationData);
			}
		}

		// Loop through the causes and their relations, only considering "to" relations so we don't get duplicates
		// (This is a slight performance loss as all causes have both ways of relations known, but it is most
		// probably negligible.)
		// As "from" and "to" are rather ambiguous terms within this project, in this case "to" is always the direction
		// where the arrow points (...at the moment :o), which is towards the root node.
		SortedSet<Cause> causes = rcaCase.causes;
		for (Cause causeTo : causes) {
			for (Relation relation : causeTo.causeRelations) {
				causeFrom = relation.causeFrom;

				// Add the root relations to the map if the to cause is the root node
				if (causeTo == rcaCase.problem && !addedRootCauses.contains(causeFrom)) {
					for (ClassificationPair fromPair : causeFrom.classifications) {
						relationData = new ClassificationRelation(
								1, causeFrom.likes.size() + causeTo.likes.size(),
								causeFrom.corrections.size() + causeTo.corrections.size()
						);
						this.addRootRelation(this.normalizer.normalizeClassification(fromPair.parent), relationData);
					}
				}

				// Add relations to all of the combinations between the classification pairs of the related
				// causes. Funky.
				for (ClassificationPair fromPair : causeFrom.classifications) {
					for (ClassificationPair toPair : causeTo.classifications) {
						// The relation strength for these two is one, as we're only handling one pair of
						// classification pairs here, and the amount of likes is the amount of likes for the two
						// causes combined.
						relationData = new ClassificationRelation(
								1, causeFrom.likes.size() + causeTo.likes.size(),
								causeFrom.corrections.size() + causeTo.corrections.size()
						);
						this.addRelation(this.normalizer.normalizePair(fromPair),
						                 this.normalizer.normalizePair(toPair),
						                 relationData);
					}
				}
			}
		}
	}


	/**
	 * Creates a relation map for a RCA case, used as a helper function
	 * @param rcaCase the RCA case
	 * @return ClassificationRelationMap
	 */
	public static ClassificationRelationMap fromCase(RCACase rcaCase) {
		ClassificationRelationMap map = new ClassificationRelationMap();
		map.loadCase(rcaCase);
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
		 * The amount of corrections of the causes classified by the classifications of the relation
		 */
		public int corrections;


		/**
		 * Constructor of the class
		 * @param strength the strength of the relation (ie. the amount of relations)
		 * @param likes the amount of likes of the causes with the relevant classifications
		 * @param corrections the amount of corrections of the causes
		 */
		public ClassificationRelation(int strength, int likes, int corrections) {
			this.strength = strength;
			this.likes = likes;
			this.corrections = corrections;
		}


		/**
		 * Adds the strength and likes of the other relation to this one
		 * @param other another ClassificationRelation
		 */
		public void add(ClassificationRelation other) {
			this.strength += other.strength;
			this.likes += other.likes;
			this.corrections += other.corrections;
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
				grandChild.addProperty("corrections", relation.corrections);
				child.add(subKey.id.toString(), grandChild);
			}
			simpleRelations.add(key.id.toString(), child);
		}

		// Construct the pair relation map
		// In pair relation maps, the keys are in the form parent:child, as JavaScript and thus JSON only allows
		// strings as object property names
		JsonObject pairRelations = new JsonObject();
		for (ClassificationPair key : this.pairRelations.keySet()) {
			child = new JsonObject();
			for (ClassificationPair subKey : this.pairRelations.get(key).keySet()) {
				relation = this.pairRelations.get(key).get(subKey);
				grandChild = new JsonObject();
				grandChild.addProperty("strength", relation.strength);
				grandChild.addProperty("likes", relation.likes);
				grandChild.addProperty("corrections", relation.corrections);
				child.add(subKey.parent.id + ":" + subKey.child.id, grandChild);
			}
			pairRelations.add(key.parent.id + ":" + key.child.id, child);
		}

		// Construct the root relation object
		JsonObject rootRelations = new JsonObject();
		for (Classification classification : this.rootRelations.keySet()) {
			child = new JsonObject();
			relation = this.rootRelations.get(classification);
			child.addProperty("strength", relation.strength);
			child.addProperty("likes", relation.likes);
			child.addProperty("corrections", relation.corrections);

			// The node IDs are encoded as strings, so coerce to string here
			rootRelations.add(classification.id.toString(), child);
		}

		// Construct the map property of the return value
		JsonObject map = new JsonObject();
		map.add("simpleRelations", simpleRelations);
		map.add("pairRelations", pairRelations);
		map.add("rootRelations", rootRelations);

		// Construct the temporary set of all classifications in the map
		// This is done by looping all pair relations, because there cannot be a simple relation that does not have
		// at least one corresponding pair relation, as all "where" classifications must have at least one "what"
		// classification paired with it.
		Set<Classification> allClassifications = new TreeSet<Classification>();
		ClassificationPair keyPair;
		for (Map.Entry<ClassificationPair, Map<ClassificationPair, ClassificationRelation>> entry :
					this.pairRelations.entrySet()) {
			// Add the parent pair to the set
			keyPair = this.normalizer.normalizePair(entry.getKey());
			allClassifications.add(keyPair.parent);
			allClassifications.add(keyPair.child);

			// Add the child pairs to the set
			for (ClassificationPair valuePair : entry.getValue().keySet()) {
				valuePair = this.normalizer.normalizePair(valuePair);
				allClassifications.add(valuePair.parent);
				allClassifications.add(valuePair.child);
			}
		}

		// Then construct the JSON object for it
		JsonObject classifications = new JsonObject();
		HashMap<Long, Integer> classificationRelevances = this.getClassificationRelevances();
		for (Classification classification : allClassifications) {
			child = new JsonObject();
			child.addProperty("id", classification.id);
			child.addProperty("title", classification.name);
			child.addProperty("dimension", classification.classificationDimension);
			child.addProperty("abbreviation", classification.abbreviation);
			child.addProperty("relevance", classificationRelevances.get(classification.id));
			child.addProperty("causeNames", (Number) null);
			classifications.add(classification.id.toString(), child);
		}

		out.add("map", map);
		out.add("classifications", classifications);
		return out.toString();
	}


	public Map<Classification, Map<Classification, ClassificationRelation>> getSimpleRelations() {
		return simpleRelations;
	}


	public Map<ClassificationPair, Map<ClassificationPair, ClassificationRelation>> getPairRelations() {
		return pairRelations;
	}
}