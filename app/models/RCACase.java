package models;

import models.enums.CompanySize;
import models.enums.RCACaseType;
import models.events.Event;

import play.cache.Cache;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.F.IndexedEvent;
import play.libs.F.Promise;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

import models.events.*;

/**
 * @author Eero Laukkanen
 */

/**
 * TODO   ENUMS
 */
@PersistenceUnit(name = "maindb")
@Entity(name = "rcacase")

public class RCACase extends Model {

	private static final String CAUSE_STREAM_NAME_IN_CACHE = "causeStream";
	private static final int NUMBER_OF_EVENTS_STORED_IN_EVENT_STREAM = 100;
	private static final String EXPIRATION_TIME_FOR_CAUSE_STREAM_IN_CACHE = "30mn";

	@Required
	@Column(name = "name")
	public String caseName;

	@Required @Min(0)
	public int caseTypeValue;

	@Required
	public String caseGoals;

	@Required @Min(0)
	public int companySizeValue;

	@Required
	public String description;

	public boolean isMultinational;

	@Required
	public String companyName;

	@Required
	public String companyProducts;

	public boolean isCasePublic;

	public Long ownerId;

	@OneToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name="problemId")
	public Cause problem;

	@OneToMany(mappedBy = "rcaCase", cascade = CascadeType.ALL)
	public Set<Cause> causes;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created", nullable = false)
	public Date created;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated", nullable = false)
	public Date updated;

	@PrePersist
	protected void onCreate() {
		Date current = new Date();
		updated = current;
		created = current;
	}

	/**
	 * Basic constructor
	 */
	public RCACase(User owner) {
		this.ownerId = owner.id;
	}

	/**
	 * Constructor for the form in create.html.
	 *
	 * @param caseName The name of the RCA case
	 * @param caseTypeValue The type of the RCA case. Enums are found in models/enums/RCACaseType.
	 * @param caseGoals
	 * @param description
	 * @param isMultinational The boolean value whether the company related to the RCA case is multinational.
	 * @param companyName The name of the company related to the RCA case.
	 * @param companySizeValue The size of the company related to the RCA case. Enums are found in models/enums/CompanySize.
	 * @param companyProducts
	 * @param isCasePublic The boolean value whether the RCA is public.
	 * @param owner The User who owns the case.
	 * 
	 * ownerId The ID of the user who creates the case.
	 * problem The Cause object that represents the problem of the RCA case.
	 */
	public RCACase(String caseName, int caseTypeValue, String caseGoals, String description, boolean isMultinational,
	               String companyName, int companySizeValue, String companyProducts, boolean isCasePublic,
	               User owner) {
		this.caseName = caseName;
		this.caseTypeValue = caseTypeValue;
		this.caseGoals = caseGoals;
		this.description = description;
		this.isMultinational = isMultinational;
		this.companyName = companyName;
		this.companySizeValue = companySizeValue;
		this.companyProducts = companyProducts;
		this.isCasePublic = isCasePublic;
		this.ownerId = owner.id;
	}

	public User getOwner() {
		return User.findById(ownerId);
	}

	public Promise<List<IndexedEvent<Event>>> nextMessages(long lastReceived) {
		CauseStream stream = this.getCauseStream();
		return stream.getStream().nextEvents(lastReceived);
	}

	public CompanySize getCompanySize() {
		return CompanySize.valueOf(companySizeValue);
	}

	public void setCompanySize(CompanySize companySize) {
		this.companySizeValue = companySize.value;
	}

	public RCACaseType getRCACaseType() {
		return RCACaseType.valueOf(caseTypeValue);
	}

	public void setRCACaseType(RCACaseType rcaCaseType) {
		this.caseTypeValue = rcaCaseType.value;
	}

	public CauseStream setCauseStream() {
		CauseStream causeEvents = new CauseStream(NUMBER_OF_EVENTS_STORED_IN_EVENT_STREAM);
		Cache.set(CAUSE_STREAM_NAME_IN_CACHE + this.id, causeEvents, EXPIRATION_TIME_FOR_CAUSE_STREAM_IN_CACHE);
		return causeEvents;
	}

	public CauseStream getCauseStream() {
		CauseStream stream = Cache.get(CAUSE_STREAM_NAME_IN_CACHE + this.id, CauseStream.class);
		if (stream == null) {
			stream = setCauseStream();
		}
		return stream;
	}

	public void deleteCause(Cause cause) {
		this.causes.remove(cause);
		this.save();
	}

}
