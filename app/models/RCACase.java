package models;

import models.enums.CompanySize;
import models.enums.RCACaseType;
import models.events.Event;

import play.cache.Cache;
import play.db.jpa.Model;
import play.libs.F;
import play.libs.F.IndexedEvent;
import play.libs.F.Promise;

import javax.persistence.*;
import java.util.List;

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

	public RCACase(String name, RCACaseType type, boolean isMultinational, String companyName, CompanySize companySize,
	               boolean isCasePublic, User owner) throws MandatoryFieldEmptyException {
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
	}

	public User getOwner() {
		return User.findById(ownerID);
	}

	public Promise<List<IndexedEvent<Event>>> nextMessages(long lastReceived) {
		return getCauseEvents().nextEvents(lastReceived);
	}

	public F.ArchivedEventStream<Event> getCauseEvents() {
		List<Event> events = Cache.get("causeEventsFor" + id, List.class);
		F.ArchivedEventStream<Event> causeEvents = new F.ArchivedEventStream<Event>(100);
		if (events != null && !events.isEmpty()) {
			for (Event event : events) {
				causeEvents.publish(event);
			}
		}
		return causeEvents;
	}

	public void saveCauseEvents(List<Event> events) {
		Cache.set("causeEventsFor" + id, events);
	}

	public void publish(Event event) {
		F.ArchivedEventStream<Event> causeEvents = getCauseEvents();
		causeEvents.publish(event);
		saveCauseEvents(causeEvents.archive());
	}
}
