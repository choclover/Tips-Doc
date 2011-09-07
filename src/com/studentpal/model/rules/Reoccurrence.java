package com.studentpal.model.rules;

import com.studentpal.model.exception.STDException;

public abstract class Reoccurrence {
  final static int DAILY    = 0x01;
  final static int WEEKLY   = 0x02;
  final static int MONTHLY  = 0x03;
  final static int YEARLY   = 0x04;
  
  public Reoccurrence getInstance(int type) throws STDException {
    Reoccurrence inst = null;
    switch (type) {
    case DAILY:
      inst = new DAILY();
      break;
    case WEEKLY:
      inst = new WEEKLY();
      break;
    case MONTHLY:
      inst = new MONTHLY();
      break;
    case YEARLY:
    default:
      throw new STDException("Unsupported REOCCURRENCE type!");
    }
    return inst;
  }
  
  final class DAILY extends Reoccurrence {
    public String getName() {
      return "DAILY";
    }
    public boolean isOccurringToday() {
      return true;
    }
  }
  
  final class WEEKLY extends Reoccurrence {
    
  }
  
  final class MONTHLY extends Reoccurrence {
    
  }
}
