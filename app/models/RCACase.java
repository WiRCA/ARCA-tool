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

	public String name;
	//TODO needed? public Set<Cause> causes;

	@Enumerated(EnumType.ORDINAL)
	public RCACaseType caseType;
	public boolean isMultinational;
	public String companyName;

	@Enumerated(EnumType.ORDINAL)
	public CompanySize companySize;
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
	 * @param companySize
	 * @param isCasePublic
	 */

	public RCACase(String name, RCACaseType type, boolean isMultinational, String companyName,
	               CompanySize companySize, boolean isCasePublic, User owner) {
		this.name = name;
		this.caseType = type;
		this.isMultinational = isMultinational;
		this.companyName = companyName;
		this.companySize = companySize;
		this.isCasePublic = isCasePublic;
		this.ownerID = owner.id;
		//TODO needed? this.causes = new TreeSet<Cause>();
		// Creating the new 'initial problem' for the RCACase with the case name.
		this.problem = new Cause(name, owner).save();

		CauseStream causeEvents = new CauseStream(100);
		Cache.set("stream", causeEvents, "30mn");
	}

	public User getOwner() {
		return User.findById(ownerID);
	}

	public Promise<List<IndexedEvent<Event>>> nextMessages(long lastReceived) {
		CauseStream stream = Cache.get("stream", CauseStream.class);
		return stream.getStream().nextEvents(lastReceived);
	}

}
