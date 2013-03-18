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

import models.enums.CommentType;
import models.enums.StatusOfCorrection;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import utils.LikableIdComparableModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Corrective action for a cause in RCA tree.
 *
 * @author Eero Laukkanen
 */
@PersistenceUnit(name = "maindb")
@Entity(name = "correction")
public class Correction extends LikableIdComparableModel {

	/**
	* the name of the corrective action
	*/
	public String name;

	/**
	* the description of the corrective action
	*/
	public String description;

	/**
	* the status of the corrective action
	*/
	public Integer statusValue = StatusOfCorrection.IDEA.value;

	/**
	* The cause that the corrective action is related to
	*/
	@ManyToOne
	@JoinColumn(name = "causeId")
	public Cause cause;

	/**
	* The comments related to this corrective action
	*/
	@OneToMany(mappedBy = "correction", cascade = CascadeType.ALL)
	@Sort(type = SortType.NATURAL)
	public SortedSet<Comment> comments;

	/**
	* Likes related to this corrective action
	*/
	@ElementCollection
	@JoinTable(name = "correctionlikes", joinColumns = {@JoinColumn(name = "correctionId", nullable = false)})
	@Column(name = "userId", nullable = false)
	public List<Long> likes;

	/**
	 * Creates a new correction with specified name and description.
	 *
	 * @param name         name of the correction
	 * @param description  description of the correction
	 * @param correctionTo the cause this correction is directed to
	 */
	public Correction(String name, String description, Cause correctionTo) {
		this.name = name;
		this.description = description;
		this.cause = correctionTo;
		this.comments = new TreeSet<Comment>();
		this.likes = new ArrayList<Long>();
	}

	/**
	 * Return users who have liked this correction.
	 *
	 * @return set of ids of users who have liked this correction
	 */
	public List<Long> getLikes() {
		return this.likes;
	}

	/**
	 * Returns the status of the correction
	 *
	 * @return StatusOfCorrection enum is returned. Null is returned if not found.
	 */
	public StatusOfCorrection getStatus() {
		return StatusOfCorrection.valueOf(statusValue);
	}

	/**
	 * Set the status of the correction
	 *
	 * @param status the status to be set
	 */
	public void setStatus(StatusOfCorrection status) {
		this.statusValue = status.value;
	}

	/**
	 * Adds a new comment to the correction.
	 *
	 * @param user        user who adds the comment
	 * @param comment     the added comment
	 * @param commentType type of the comment - neutral, negative or positive
	 */
	public void addComment(User user, String comment, CommentType commentType) {
		Comment newComment = new Comment(user, this, comment, commentType);
		this.comments.add(newComment);
		this.save();
	}

	/**
	 * Remove a comment.
	 *
	 * @param comment the comment to be removed
	 */
	public void removeComment(Comment comment) {
		this.comments.remove(comment);
		this.save();
		comment.delete();
	}
}
