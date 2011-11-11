package models.events;

public abstract class Event {

	public final String type;
	public final Long timestamp;

	public Event(String type) {
		this.type = type;
		this.timestamp = System.currentTimeMillis();
	}

}



