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

import java.util.List;

/**
 * This class represents the Classification Table. It shows how many of the causes contain certain ClassificationPair.
 * Each table cell is represented as TableCellObject
 * @see TableCellObject
 */
public class ClassificationTable {

	public TableCellObject[][] tableCells;
	public List<String> rowNames;
	public List<String> colNames;

	public ClassificationTable(int x, int y) {
		tableCells = new TableCellObject[x][y];
		for (int i = 0; i < tableCells.length; i++) {
			for (int j = 0; j < tableCells[i].length; j++) {
				tableCells[i][j] = new TableCellObject();
			}
		}
	}

	/**
	 * This is a representation of table cell.
	 */
	public class TableCellObject {
		public int numberOfCauses = 0;
		/**
		 * Tells the proportion of causes where ClassificationPair is found from all causes
		 */
		public double percentOfCauses = 0;
		public int numberOfProposedCauses = 0;
		/**
		 * Tells the proportion of causes that have likes and where ClassificationPair is found from all causes
		 */
		public double percentOfProposedCauses = 0;
		public int numberOfCorrectionCauses = 0;
		/**
		 * Tells the proportion of causes that have corrections and where ClassificationPair is found from all causes
		 */
		public double percentOfCorrectionCauses = 0;
	}
}