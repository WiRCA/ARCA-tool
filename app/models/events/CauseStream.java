package models.events;

import java.util.*;
import play.db.jpa.Model;
import play.libs.*;
import play.libs.F.*;
import java.io.Serializable;


public class CauseStream implements Serializable {
    
    public final ArchivedEventStream<Event> eventStream;
    
    public CauseStream(long size) {
        this.eventStream = new ArchivedEventStream<Event>(100);
    }
    
    public ArchivedEventStream<Event> getStream() {
      return eventStream;
    }
    
}



