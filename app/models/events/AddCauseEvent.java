package models.events;

import java.util.*;
import play.db.jpa.Model;
import play.libs.*;
import play.libs.F.*;
import models.Cause;

public class AddCauseEvent extends Event {
    
  final public String user;
  final public String text;
  
  public AddCauseEvent(Cause cause, String causeFrom) {
      super("addcauseevent");
      this.user = cause.getCreator().toString();
      this.text = cause.name;
  }
    
}



