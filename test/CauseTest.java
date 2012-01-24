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

import models.Cause;
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
public class CauseTest extends GenericRCAUnitTest {

	@Test
	public void getCreatorAndParentTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user);
		assertTrue(cause1.getCreator().equals(user));
		assertNull(cause1.getParent());
	}

	@Test
	public void updateCauseTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user);
		Date date1 = cause1.rcaCase.updated;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Logger.fatal("Sleep failed in a test");
			assert true;
		}
		cause1.name = "test cause1 modified";
		cause1.save();
		assertTrue(cause1.rcaCase.updated.after(date1));
	}

	@Test
	public void addCauseTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user).save();
		Cause cause2 = new Cause(testCase, "test cause2", user).save();
		cause1.addCause(cause2);
		assertTrue(cause2.isChildOf(cause1));
		Cause cause3 = cause1.addCause("test cause3", user);
		assertTrue(cause3.isChildOf(cause1));
	}

	@Test
	public void isParentTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user).save();
		Cause cause2 = new Cause(testCase, "test cause2", user).save();
		Cause cause3 = new Cause(testCase, "test cause3", user).save();
		cause1.addCause(cause2);
		assertTrue(cause2.isChildOf(cause1));
		assertTrue(cause2.getParent().equals(cause1));
		cause3.addCause(cause2);
		assertFalse(cause2.isChildOf(cause3));
	}

	@Test
	public void anotherIsParentTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user).save();
		Cause cause2 = new Cause(testCase, "test cause2", user).save();
		Cause cause3 = new Cause(testCase, "test cause3", user).save();
		cause1.addCause(cause2);
		assertTrue(cause2.isChildOf(cause1));
		assertTrue(cause2.getParent().equals(cause1));
		cause1.addCause(cause3);
		assertTrue(cause3.isChildOf(cause1));
		assertTrue(cause3.getParent().equals(cause1));
		cause3.addCause(cause2);
		assertTrue(cause2.isChildOf(cause1));
		assertTrue(cause2.getParent().equals(cause1));
		assertTrue(cause3.isChildOf(cause1));
		assertTrue(cause3.getParent().equals(cause1));
	}

	@Test
	public void getCausesAndRelationsTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user).save();
		Cause cause2 = new Cause(testCase, "test cause2", user).save();
		Cause cause3 = new Cause(testCase, "test cause3", user).save();
		Cause cause4 = new Cause(testCase, "test cause4", user).save();
		cause1.addCause(cause2);
		cause1.addCause(cause3);
		cause2.addCause(cause4);
		cause1.addCause(cause4);
		Set<Cause> causes = cause1.getCauses();
		Set<Cause> relations = cause1.getRelations();
		assertTrue(causes.contains(cause2));
		assertTrue(causes.contains(cause3));
		assertFalse(causes.contains(cause4));
		assertTrue(relations.contains(cause4));
		assertFalse(relations.contains(cause2));
	}

	@Test
	public void deleteTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user).save();
		Cause cause2 = new Cause(testCase, "test cause2", user).save();
		Cause cause3 = new Cause(testCase, "test cause3", user).save();
		cause1.addCause(cause2);
		cause2.addCause(cause3);
		cause2.deleteCause();
		assertFalse(cause1.getCauses().contains(cause2));
		assertTrue(cause3.effectRelations.isEmpty());
	}

	@Test
	public void correctionTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user).save();
		Correction correction = cause1.addCorrection("new correction", "correction for a cause");
		assertTrue(correction.cause.equals(cause1));
		assertTrue(cause1.corrections.contains(correction));
		Correction correction2 = Correction.find("byName", "new correction").first();
		assertNotNull(correction2);
		cause1.removeCorrection(correction);
		Correction correction3 = Correction.find("byName", "new correction").first();
		assertNull(correction3);
		correction = cause1.addCorrection("new correction", "correction for a cause");
		correction.like(user);
		assertEquals(1, correction.likes.size());
		correction.dislike(user);
		assertEquals(0, correction.likes.size());
		correction.addComment(user, "hello world", CommentType.NEUTRAL);
		Comment findComment = Comment.find("byComment", "hello world").first();
		assertEquals(user.id, findComment.creatorId);
		correction.removeComment(findComment);
		int commentsSize = Comment.find("byComment", "hello world").fetch().size();
		assertEquals(0, commentsSize);

	}

	@Test
	public void circularCausesTest() {
		Cause cause1 = new Cause(testCase, "test cause1", user).save();
		Cause cause2 = cause1.addCause("test cause2", user).save();
		cause2.addCause(cause1);
		assertTrue(cause1.getCauses().contains(cause2));
		assertTrue(cause2.getCauses().contains(cause1));
		testCase.deleteCause(cause2);
		assertFalse(cause1.getCauses().contains(cause2));
	}

	@Test
	public void likeTest() {
		Cause cause = new Cause(testCase, "test cause", user).save();
		assertEquals(0, cause.likes.size());
		cause.like(user);
		assertEquals(1, cause.likes.size());
		cause.like(user);
		assertEquals(2, cause.likes.size());
		cause.dislike(user);
		assertEquals(1, cause.likes.size());
	}

	@Test
	public void coordinateTest() {
		Cause cause = new Cause(testCase, "test cause", user).save();
		assertFalse(cause.areCoordinatesSet());
		cause.xCoordinate = 0;
		cause.save();
		assertFalse(cause.areCoordinatesSet());
		cause.yCoordinate = 0;
		cause.save();
		assertTrue(cause.areCoordinatesSet());
	}
}
