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

package utils;

import models.User;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Abstract class that is comparable and provides 'like'-feature.
 *
 * @author Eero Laukkanen
 */
public abstract class LikableIdComparableModel extends IdComparableModel {

	public abstract SortedSet<Long> getLikes();

	/**
	 * Returns all users who have liked this object.
	 *
	 * @return set of all users who have liked this object
	 */
	public SortedSet<User> getUsersWhoLike() {
		Collection<Long> likes;
		likes = getLikes();
		SortedSet<User> returnedLikes = new TreeSet<User>();
		for (Long like : likes) {
			returnedLikes.add(User.<User>findById(like));
		}
		return returnedLikes;
	}

	/**
	 * Called when user likes the correction.
	 *
	 * @param user the user who likes the correction
	 */
	public void like(User user) {
		this.getLikes().add(user.id);
		this.save();
	}

	/**
	 * Called when user dislikes the correction.
	 *
	 * @param user the user who dislikes the correction
	 */
	public void dislike(User user) {
		this.getLikes().remove(user.id);
		this.save();
	}

}
