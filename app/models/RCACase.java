package models;

import models.enums.CompanySize;
import models.enums.RCACaseType;
import models.events.Event;

import play.cache.Cache;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.db.jpa.Model;
import play.libs.F.IndexedEvent;
import play.libs.F.Promise;

import javax.persistence.*;
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

	@Required
	@Column(name = "name")
	public String caseName;

	@Required @Min(0)
	@Column(name = "case_type_value")
	public int caseTypeValue;

	@Required
	@Column(name = "case_goals")
	public String caseGoals;

	@Required @Min(0)
	@Column(name = "company_size_value")
	public int companySizeValue;

	@Required
	@Column(name = "description")
	public String description;

	@Column(name = "is_multinational")
	public boolean isMultinational;

	@Required
	@Column(name = "company_name")
	public String companyName;

	@Required
	@Column(name = "company_products")
	public String companyProducts;

	@Column(name = "is_case_public")
	public boolean isCasePublic;

	@Required
	@Column(name = "owner_id")
	public Long ownerId;

	@OneToOne
	public Cause problem;

	@OneToMany(mappedBy = "rcaCase")
	public Set<Cause> causes;

	/**
	 * Constructor for the form in create.html.
	 *
	 * @param caseName The name of the RCA case
	 * @param caseTypeValue The type of the RCA case. Enums are found in models/enums/RCACaseType.
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

	public RCACase(@Valid String caseName, @Valid int caseTypeValue, @Valid String caseGoals,
	               @Valid String description,
	               boolean isMultinational,
	               @Valid String companyName,
	               @Valid int companySizeValue, @Valid String companyProducts, boolean isCasePublic,
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
		CauseStream causeEvents = new CauseStream(100);
		Cache.set(CAUSE_STREAM_NAME_IN_CACHE + this.id, causeEvents, "30mn");
		return causeEvents;
	}

	public CauseStream getCauseStream() {
		CauseStream stream = Cache.get(CAUSE_STREAM_NAME_IN_CACHE + this.id, CauseStream.class);
		if (stream == null) {
			stream = setCauseStream();
		}
		return stream;
	}

}
