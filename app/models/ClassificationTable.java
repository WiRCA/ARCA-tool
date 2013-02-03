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

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Classification Table. It shows how many of the causes contain certain ClassificationPair.
 * Each table cell is represented as TableCell
 * @see models.ClassificationTable.TableCell
 * @todo change to support an arbitrary amount of cases by loading them one at a time
 */
public class ClassificationTable {
	public ArrayList<ArrayList<TableCell>> tableCells;
	public List<String> rowNames;
	public List<String> colNames;


	public ClassificationTable(int width, int height) {
		tableCells = new ArrayList<ArrayList<TableCell>>(height);
		ArrayList<TableCell> cell;
		for (int y = 0; y < height; y++) {
			cell = new ArrayList<TableCell>(width);
			for (int x = 0; x < height; x++) {
				cell.add(new TableCell());
			}
			tableCells.add(cell);
		}
		this.colNames = new ArrayList<String>();
		this.rowNames = new ArrayList<String>();
	}


	/**
	 * Calculates percentages and total percentages for the statistics in the table
	 */
	public void calculatePercentages(int numberOfClassificationPairs) {
		for (int i = 0; i < this.tableCells.size() - 1; i++) {
			TableCell totalColumnObject = this.tableCells.get(i).get(this.tableCells.get(i).size() - 1);
			for (int j = 0; j < this.tableCells.get(i).size() - 1; j++) {
				TableCell totalRowObject = this.tableCells.get(this.tableCells.size() - 1).get(j);
				TableCell currentCell = this.tableCells.get(i).get(j);

				currentCell.percentOfCauses = (double)currentCell.numberOfCauses / numberOfClassificationPairs * 100.0;
				currentCell.percentOfProposedCauses = (double)currentCell.numberOfProposedCauses / numberOfClassificationPairs * 100.0;
				currentCell.percentOfCorrectionCauses = (double)currentCell.numberOfCorrectionCauses / numberOfClassificationPairs * 100.0;
				totalColumnObject.percentOfCauses += currentCell.percentOfCauses;
				totalColumnObject.percentOfProposedCauses += currentCell.percentOfProposedCauses;
				totalColumnObject.percentOfCorrectionCauses += currentCell.percentOfCorrectionCauses;
				totalRowObject.percentOfCauses += currentCell.percentOfCauses;
				totalRowObject.percentOfProposedCauses += currentCell.percentOfProposedCauses;
				totalRowObject.percentOfCorrectionCauses += currentCell.percentOfCorrectionCauses;
			}
		}
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