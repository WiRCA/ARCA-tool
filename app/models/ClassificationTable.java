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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import play.Logger;

import java.util.*;

/**
 * This class represents the Classification Table. It shows how many of the causes contain certain ClassificationPair.
 * Each table cell is represented as TableCell
 * @see models.ClassificationTable.TableCell
 */
public class ClassificationTable {
	public List<String> rowNames;
	public List<String> colNames;
	private Map<Classification, HashMap<Classification, TableCell>> table;
	private HashMap<Classification, Double> rowTotals;
	private HashMap<Classification, Double> columnTotals;
	private ClassificationNormalizer normalizer;


	public ClassificationTable() {
		this.table = new HashMap<Classification, HashMap<Classification, TableCell>>();
		this.colNames = new ArrayList<String>();
		this.rowNames = new ArrayList<String>();
		this.normalizer = new ClassificationNormalizer();
	}


	/**
	 * Generates a 2D array of TableCells for the frontend.
	 * @return a 2D array of TableCells, with the last row and last column of each row being the relevant total
	 */
	public List<ArrayList<TableCell>> generateArray() {
		// The table height is the amount of "where" classifications, plus one for the total row
		int height = this.table.size() + 1;

		// The table width is the total amount of unique "what" classifications found, plus one
		List<Classification> whatClassifications = this.normalizer.getWhatClassifications();
		int width = whatClassifications.size() + 1;

		// Populate the table with empty TableCell objects
		List<ArrayList<TableCell>> out = new ArrayList<ArrayList<TableCell>>(height);
		for (int y = 0; y < height; y++) {
			ArrayList<TableCell> row = new ArrayList<TableCell>(width);
			for (int x = 0; x < width; x++) {
				row.add(new TableCell());
			}
			out.add(row);
		}

		// Create the column and row mappings, ie. sorted lists of classifications
		// Rows
		List<Classification> rowClassifications = new ArrayList<Classification>(height);
		rowClassifications.addAll(this.table.keySet());
		Collections.sort(rowClassifications);

		// Columns
		List<Classification> columnClassifications = new ArrayList<Classification>(width);
		columnClassifications.addAll(whatClassifications);
		Collections.sort(columnClassifications);

		// Reference for the total row (the last row)
		List<TableCell> totalRow = out.get(height - 1);

		// Get the total amount of classification pairs found
		int total = 0;
		for (Map.Entry<Classification, HashMap<Classification, TableCell>> entry1 : this.table.entrySet()) {
			for (Map.Entry<Classification, TableCell> entry2 : entry1.getValue().entrySet()) {
				total += entry2.getValue().numberOfCauses;
			}
		}

		// Loop through all of our "where" classifications as rows
		for (int y = 0; y < height - 1; y++) {
			// Get the actual list of output cells for this row
			List<TableCell> currentRow = out.get(y);

			// Get a reference to the row total cell (the last cell)
			TableCell rowTotalCell = currentRow.get(width - 1);

			// Get the corresponding classification for this row
			Classification rowKey = rowClassifications.get(y);

			// Add the classification's name to the row name array
			this.rowNames.add(rowKey.name);

			// Loop through all of our "what" classifications as columns
			for (int x = 0; x < width - 1; x++) {
				// Get the corresponding classification for this column
				Classification columnKey = columnClassifications.get(x);

				// If this is the first outer iteration, add the column classification's name to the column name array
				if (y == 0) {
					this.colNames.add(columnKey.name);
				}

				// If there is no data for this classification pair, bail
				if (!this.table.get(rowKey).containsKey(columnKey)) {
					continue;
				}

				// Get a reference to this column's total cell
				TableCell columnTotalCell = totalRow.get(x);

				// Get the source and target cells for this pair of classifications
				TableCell sourceCell = this.table.get(rowKey).get(columnKey);
				TableCell targetCell = currentRow.get(x);

				// Update the target cell
				targetCell.percentOfCauses = (double) sourceCell.numberOfCauses / total * 100.0;
				targetCell.percentOfProposedCauses = (double) sourceCell.numberOfProposedCauses / total * 100.0;
				targetCell.percentOfCorrectionCauses = (double) sourceCell.numberOfCorrectionCauses / total * 100.0;

				// Update the row total
				rowTotalCell.percentOfCauses += targetCell.percentOfCauses;
				rowTotalCell.percentOfProposedCauses += targetCell.percentOfProposedCauses;
				rowTotalCell.percentOfCorrectionCauses += targetCell.percentOfCorrectionCauses;

				// Update the column total
				columnTotalCell.percentOfCauses += targetCell.percentOfCauses;
				columnTotalCell.percentOfProposedCauses += targetCell.percentOfProposedCauses;
				columnTotalCell.percentOfCorrectionCauses += targetCell.percentOfCorrectionCauses;
			}
		}

		// Append the total row and column names
		this.rowNames.add(play.i18n.Messages.get("classificationTablePage.total"));
		this.colNames.add(play.i18n.Messages.get("classificationTablePage.total"));

		return out;
	}


	/**
	 * Load an RCA case's data into the table
	 * @param rcaCase the RCA case to load
	 * @see RCACase
	 */
	public void loadCase(RCACase rcaCase) {
		// Loop through all classification pairs for all causes
		for (Cause cause : rcaCase.causes) {
			SortedSet<ClassificationPair> pairs = cause.getClassifications();

			for (ClassificationPair pair : pairs) {
				pair = this.normalizer.normalizePair(pair);

				HashMap<Classification, TableCell> row;
				if (this.table.containsKey(pair.parent)) {
					row = this.table.get(pair.parent);
				} else {
					row = new HashMap<Classification, TableCell>();
					this.table.put(pair.parent, row);
				}

				TableCell cell;
				if (row.containsKey(pair.child)) {
					cell = row.get(pair.child);
				} else {
					cell = new TableCell();
					row.put(pair.child, cell);
				}

				// Increase the cause counter for the classification pair
				cell.numberOfCauses++;

				// Increase the proposed counter for the classification pair if cause is liked
				if (cause.countLikes() > 0) {
					cell.numberOfProposedCauses++;
				}

				// Increase the correction counter for the classification pair if cause has correction proposal
				if (cause.corrections.size() > 0) {
					cell.numberOfCorrectionCauses++;
				}
			}
		}
	}


	/**
	 * Generates a JSON string that represents the table, for use with the frontend JavaScript.
	 * @return JSON string
	 */
	public String toJson() {
		List<ArrayList<TableCell>> rawTable = this.generateArray();

		JsonObject temp;
		JsonArray table = new JsonArray();
		for (ArrayList<TableCell> row : rawTable) {
			JsonArray rowJson = new JsonArray();
			for (TableCell cell : row) {
				temp = new JsonObject();
				temp.addProperty("percentOfCauses", String.valueOf(cell.percentOfCauses));
				temp.addProperty("percentOfProposedCauses", String.valueOf(cell.percentOfProposedCauses));
				temp.addProperty("percentOfCorrectionCauses", String.valueOf(cell.percentOfCorrectionCauses));
				rowJson.add(temp);
			}
			table.add(rowJson);
		}

		JsonArray rowNames = new JsonArray();
		for (String name : this.rowNames) {
			rowNames.add(new JsonPrimitive(name));
		}

		JsonArray colNames = new JsonArray();
		for (String name : this.colNames) {
			colNames.add(new JsonPrimitive(name));
		}

		JsonObject out = new JsonObject();
		out.add("table", table);
		out.add("rowNames", rowNames);
		out.add("colNames", colNames);
		return out.toString();
	}


	/**
	 * This is a representation of table cell.
	 */
	public class TableCell {
		public int numberOfCauses = 0;
		public int numberOfProposedCauses = 0;
		public int numberOfCorrectionCauses = 0;

		/**
		 * Tells the proportion of causes where ClassificationPair is found from all causes
		 */
		public double percentOfCauses = 0;

		/**
		 * Tells the proportion of causes that have likes and where ClassificationPair is found from all causes
		 */
		public double percentOfProposedCauses = 0;

		/**
		 * Tells the proportion of causes that have corrections and where ClassificationPair is found from all causes
		 */
		public double percentOfCorrectionCauses = 0;
	}
}