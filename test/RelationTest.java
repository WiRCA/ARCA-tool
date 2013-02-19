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

import models.Cause;
import models.Relation;
import models.Comment;
import models.Correction;
import models.enums.CommentType;
import org.junit.Test;
import play.Logger;

import java.util.Date;
import java.util.Set;

/**
 * @author Eero Laukkanen
 */
public class RelationTest extends GenericRCAUnitTest {

	@Test
	public void createRelationTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user).save();
		Cause cause2 = new Cause(testCase, "test cause2", user).save();
		cause1.addCause(cause2);
		Relation relation = new Relation(cause1, cause2);
		assertTrue(cause1 == relation.causeFrom);
		assertTrue(cause2 == relation.causeTo);
	}

	@Test
	public void deleteRelationTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user).save();
		Cause cause2 = new Cause(testCase, "test cause2", user).save();
		cause1.addCause(cause2);
		Relation relation = new Relation(cause1, cause2);
		relation.delete();
		assertTrue(cause1.getRelations().isEmpty());
		assertTrue(cause2.getRelations().isEmpty());
	}
}
