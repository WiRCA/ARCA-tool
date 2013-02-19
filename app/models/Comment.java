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
import utils.IdComparableModel;

import javax.persistence.*;
import java.util.Date;

/**
 * Class for comments for corrections. Comments can be tagged as neutral, negative or positive.
 *
 * @author Eero Laukkanen
 */

@PersistenceUnit(name = "maindb")
@Entity(name = "correctioncomments")
public class Comment extends IdComparableModel {

	
	/**
	* The corrective action that the comment is related to
	*/
	@ManyToOne
	@JoinColumn(name = "correctionId")
	public Correction correction;

	/**
	* Id of the author user
	*/
	@Column(name = "userId")
	public Long creatorId;

	/**
	* The created date of the comment
	*/
	@Temporal(TemporalType.TIMESTAMP)
	public Date created;

	/**
	* The updated date of the comment
	*/
	@Temporal(TemporalType.TIMESTAMP)
	public Date updated;

	/**
	* The comment type of the comment
	*/
	@Column(name = "tag")
	public Integer commentType;

	/**
	* The text of the comment
	*/
	@Lob
	public String comment;

	/**
	 * Creates a new comment. Timestamps (created, updated) are updated within the database.
	 *
	 * @param creator    the creator of the comment
	 * @param correction the correction that the comment is made for
	 * @param comment    the comment
	 * @param type       type of the comment - neutral, negative or positive
	 */
	public Comment(User creator, Correction correction, String comment, CommentType type) {
		this.correction = correction;
		this.creatorId = creator.id;
		this.comment = comment;
		this.commentType = type.value;
	}

	/**
	 * Returns the creator of this comment.
	 *
	 * @return the creator of this comment
	 */
	public User getCreator() {
		User returnedUser = null;
		if (creatorId != null) {
			returnedUser = User.findById(creatorId);
		}
		return returnedUser;
	}

}
