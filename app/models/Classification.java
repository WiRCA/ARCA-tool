package models;

import models.RCACase;
import models.enums.ClassificationDimension;
import utils.IdComparableModel;

import javax.persistence.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: jaffa
 * Date: 10/8/12
 * Time: 5:10 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Nimi
 * Selite
 * Dimensio
 * Lyhenne
 *
 */
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


	public int classificationDimension;


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
