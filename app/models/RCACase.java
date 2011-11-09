package models;

import models.enums.CompanySize;
import models.enums.RCACaseType;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.TreeSet;

import play.libs.F.*;
import java.util.List;
import models.events.*;

/**
 * @author Eero Laukkanen
 */


/**
 * TODO   ENUMS
 */
@PersistenceUnit(name="maindb")
@Entity(name = "rcacase")

public class RCACase extends Model {

	public String name;
	public TreeSet<Cause> causes;
	
  @Transient
	public final ArchivedEventStream<Event> causeEvents = new ArchivedEventStream<Event>(100);
	
	public Promise<List<IndexedEvent<Event>>> nextMessages(long lastReceived) {
      return causeEvents.nextEvents(lastReceived);
  }

	@Enumerated(EnumType.ORDINAL)
	public RCACaseType caseType;
	public boolean isMultinational;
	public String companyName;

	@Enumerated(EnumType.ORDINAL)
	public CompanySize companySize;
	public boolean isCasePublic;
	public Long owner_id;

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
		if (name.trim().length() == 0) {
			throw new MandatoryFieldEmptyException("Name field is empty!");
		}
		else if (companyName.trim().length() == 0) {
			throw new MandatoryFieldEmptyException("Company name field is empty!");
		}

		this.name = name;
		this.caseType = type;
		this.isMultinational = isMultinational;
		this.companyName = companyName;
		this.companySize = companySize;
		this.isCasePublic = isCasePublic;
		this.owner_id = owner.id;
		this.causes = new TreeSet<Cause>();
		// Creating the new 'initial problem' for the RCACase with the case name.
		this.problem = new Cause(name, owner).save();
	}

	public User getOwner() {
		return User.findById(owner_id);
	}

}
