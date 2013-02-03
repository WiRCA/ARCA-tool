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

import models.ClassificationTable;
import org.junit.Test;

public class ClassificationTableTest extends GenericRCAUnitTest {
	@Test
	public void createClassificationTableTest() {
		ClassificationTable table = new ClassificationTable(10, 12);
		for (int i = 0; i < table.tableCells.size(); i++) {
			for (int j = 0; j < table.tableCells.get(0).size(); j++) {
				ClassificationTable.TableCell cell = table.tableCells.get(i).get(j);
				assertTrue(cell.percentOfCorrectionCauses == 0);
				assertTrue(cell.numberOfProposedCauses == 0);
				assertTrue(cell.percentOfCauses == 0);
				assertTrue(cell.percentOfProposedCauses == 0);
				assertTrue(cell.numberOfCauses == 0);
				assertTrue(cell.numberOfCorrectionCauses == 0);
			}
		}
	}

	@Test
	public void createAndCheckClassificationTableTest() {
		ClassificationTable table = new ClassificationTable(10, 12);
		for (int i = 0; i < table.tableCells.size(); i++) {
			for (int j = 0; j < table.tableCells.get(0).size(); j++) {
				ClassificationTable.TableCell cell = table.tableCells.get(i).get(j);
				if (i == 5 && j == 1) {
					cell.numberOfCauses = 5;
				}
				if (i == 7 && j == 2) {
					cell.percentOfCauses = 2;
				}
				if (i == 1 && j == 3) {
					cell.numberOfCorrectionCauses = 10;
				}
			}
		}
		for (int i = 0; i < table.tableCells.size(); i++) {
			for (int j = 0; j < table.tableCells.get(0).size(); j++) {
				ClassificationTable.TableCell cell = table.tableCells.get(i).get(j);
				if (i == 5 && j == 1) {
					assertTrue(cell.numberOfCauses == 5);
				}
				if (i == 7 && j == 2) {
					assertTrue(cell.percentOfCauses == 2);
				}
				if (i == 1 && j == 3) {
					assertTrue(cell.numberOfCorrectionCauses == 10);
				}
			}
		}
	}

}
