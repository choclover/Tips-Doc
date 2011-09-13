package com.studentpal.model.rules;

import java.util.Calendar;

import com.studentpal.model.exception.STDException;

public abstract class Recurrence {
  public final static int DAILY    = 0x01;
  public final static int WEEKLY   = 0x02;
  public final static int MONTHLY  = 0x03;
  public final static int YEARLY   = 0x04;
  
  Object recurValue = null;
  
  public static Recurrence getInstance(int type) throws STDException {
    Recurrence inst = null;
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
      throw new STDException("Unsupported recurrence type!");
    }
    return inst;
  }
  
  public abstract void setRecurValue(Object recureVal) throws STDException;
  public abstract boolean isOccurringToday();
  
  /*
   * Inner class
   */
  static final class DAILY extends Recurrence {
    public String getName() {
      return "DAILY";
    }
    
    public void setRecurValue(Object recureVal) {
      recurValue = recureVal;
    }

    public boolean isOccurringToday() {
      return true;
    }
  }
  
  static final class WEEKLY extends Recurrence {
    public String getName() {
      return "WEEKLY";
    }
    
    public void setRecurValue(Object recurVal) throws STDException {
      if (recurVal!=null && recurVal instanceof Integer) {
        this.recurValue = recurVal;
      } else {
        throw new STDException("Invalid recurrence value");
      }
    }

    public boolean isOccurringToday() {
      Calendar c = Calendar.getInstance();
      int weekDay = c.get(Calendar.DAY_OF_WEEK);
      int recur = (recurValue!=null) ? ((Integer)recurValue).intValue() : 0;
      
      return (weekDay & recur) != 0 ;
    }
  }
  
  static final class MONTHLY extends Recurrence {
    public String getName() {
      return "MONTHLY";
    }
    
    public void setRecurValue(Object recurVal) throws STDException {
      if (recurVal!=null && recurVal instanceof int[]) {
        this.recurValue = recurVal;
      } else {
        throw new STDException("Invalid recurrence value");
      }
    }

    public boolean isOccurringToday() {
      boolean result = false;
      Calendar c = Calendar.getInstance();
      int mDay = c.get(Calendar.DAY_OF_MONTH);
      for (int date : (int[])recurValue) {
        if (mDay == date) {
          result = true;  break;
        }
      }
      return result;
    }
  }
}
