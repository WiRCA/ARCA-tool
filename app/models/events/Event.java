package models.events;

import java.util.*;
import play.db.jpa.Model;
import play.libs.*;
import play.libs.F.*;

public abstract class Event {
    
    final public String type;
    final public Long timestamp;
    
    public Event(String type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }
    
}



