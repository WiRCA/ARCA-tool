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

import utils.IdComparableModel;

import javax.persistence.*;
import java.util.Date;


@PersistenceUnit(name = "maindb")
@Entity(name = "classification")
public class Classification extends IdComparableModel {
	/**
	 * The name of the classification
	 */
	public String name;

	/**
	 * The explanation of the classification
	 */
	public String explanation;

	/**
	 * The shortname of the classification
	 */
	//@Column(unique=true)
	public String abbreviation;

	/**
	 * The rca case that the cause belongs to
	 */
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "rcaCaseId")
	public RCACase rcaCase;

	/**
	 * the id of the creator user
	 */
	public Long creatorId;

	/**
	 * The updated dat of the cause
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated", nullable = false)
	private Date updated;

	@Basic
	public int classificationDimension;

	@ManyToOne
	private Cause cause;

	/**
	 * This method is called when the cause is created
	 */
	@PrePersist
	protected void onCreate() {
		updated = new Date();
		rcaCase.updated = updated;
	}

	/**
	 * This method is called when the cause is updated
	 */
	@PreUpdate
	protected void onUpdate() {
		updated = new Date();
		rcaCase.updated = updated;
	}

	public Classification(RCACase rcaCase, String name, User creator, int classificationDimension,
	                      String explanation, String abbreviation) {
		this.rcaCase = rcaCase;
		this.name = name;
		if (creator != null) {
			this.creatorId = creator.id;
		}
		this.classificationDimension = classificationDimension;
		this.explanation = explanation;
		this.abbreviation = abbreviation;
	}

}
