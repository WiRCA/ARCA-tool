package models;

import models.enums.CompanySize;
import models.enums.RCACaseType;
import models.events.Event;

import play.cache.Cache;
import play.db.jpa.Model;
import play.libs.F.IndexedEvent;
import play.libs.F.Promise;

import javax.persistence.*;
import java.util.List;

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

	public String name;
	//TODO needed? public Set<Cause> causes;

	@Column(name = "case_type_value")
	public int caseTypeValue;

	@Column(name = "company_size_value")
	public int companySizeValue;

	@Column(name = "is_multinational")
	public boolean isMultinational;

	@Column(name = "company_name")
	public String companyName;

	@Column(name = "is_case_public")
	public boolean isCasePublic;

	@Column(name = "owner_id")
	public Long ownerID;

	@OneToOne
	public Cause problem;

	/**
	 * Constructor for the form in create.html.
	 *
	 * @param name
	 * @param type
	 * @param isMultinational
	 * @param companyName
	 * @param companySizeValue
	 * @param isCasePublic
	 */

	public RCACase(String name, int type, boolean isMultinational, String companyName,
	               int companySizeValue, boolean isCasePublic, User owner) {
		this.name = name;
		this.caseTypeValue = type;
		this.isMultinational = isMultinational;
		this.companyName = companyName;
		this.companySizeValue = companySizeValue;
		this.isCasePublic = isCasePublic;
		this.ownerID = owner.id;
		//TODO needed? this.causes = new TreeSet<Cause>();
		// Creating the new 'initial problem' for the RCACase with the case name.
		this.problem = new Cause(name, owner).save();
	}

	public User getOwner() {
		return User.findById(ownerID);
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

	public CauseStream setCauseSteam() {
		CauseStream causeEvents = new CauseStream(100);
		Cache.set(CAUSE_STREAM_NAME_IN_CACHE + this.id, causeEvents, "30mn");
		return causeEvents;
	}

	public CauseStream getCauseStream() {
		CauseStream stream = Cache.get(CAUSE_STREAM_NAME_IN_CACHE + this.id, CauseStream.class);
		if (stream == null) {
			stream = setCauseSteam();
		}
		return stream;
	}

}
