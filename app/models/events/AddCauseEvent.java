package models.events;

import java.util.*;
import play.db.jpa.Model;
import play.libs.*;
import play.libs.F.*;
import models.Cause;

public class AddCauseEvent extends Event {
    
  final public String user;
  final public String text;
  final public String causeFrom;
  final public String causeTo;
  
  public AddCauseEvent(Cause cause, String causeFrom) {
      super("addcauseevent");
      this.causeTo = Long.toString(cause.id);
      //TODO
      this.user = cause.getCreator().toString();
      this.text = cause.name;
      this.causeFrom = causeFrom;
  }
    
}



