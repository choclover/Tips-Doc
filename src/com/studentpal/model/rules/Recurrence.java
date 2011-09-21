package com.studentpal.model.rules;

import java.util.Calendar;

import com.studentpal.engine.Event;
import com.studentpal.model.exception.STDException;
import com.studentpal.util.logger.Logger;

public abstract class Recurrence {
  public final static int DAILY    = Event.RECUR_TYPE_DAILY;
  public final static int WEEKLY   = Event.RECUR_TYPE_WEEKLY;
  public final static int MONTHLY  = Event.RECUR_TYPE_MONTHLY;
  public final static int YEARLY   = Event.RECUR_TYPE_YEARLY;
  
  Object recurValue = null;
  int recurType = DAILY;
  
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
  
  public abstract String getName();
  public abstract void setRecurValue(Object recureVal) throws STDException;
  public abstract boolean isOccurringToday();
  public abstract String toString();
  
  public int getRecurType() {
    return recurType;
  }
  
  /**
   * Inner class
   */
  static final class DAILY extends Recurrence {
    public DAILY() {
      recurType = DAILY;
    }
    
    public String getName() {
      return "DAILY";
    }
    
    public void setRecurValue(Object recureVal) {
      recurValue = recureVal;
    }

    public boolean isOccurringToday() {
      return true;
    }
    
    public String toString() {
      return "";
    }
  }
  
  static final class WEEKLY extends Recurrence {
    public WEEKLY() {
      recurType = WEEKLY;
    }
    
    public String getName() {
      return "WEEKLY";
    }
    
    public void setRecurValue(Object recurVal) throws STDException {
      if (recurVal==null || !(recurVal instanceof Integer) ) {
        throw new STDException("Illegal recurrence value");
      } else {
        Integer val = (Integer)recurVal;
        if (val<1 || val>0x7F) {
          throw new STDException("Recurrence value out of valid range: "+Integer.toHexString(val));
        } else {
          Logger.d("Setting "+getName()+ " RecurValue to: " + Integer.toHexString(val));
          this.recurValue = recurVal; 
        }
      }
    }

    public boolean isOccurringToday() {
      if (recurValue == null) return false;
      
      Calendar c = Calendar.getInstance();
      int weekDay = c.get(Calendar.DAY_OF_WEEK);
      weekDay = 1 << (weekDay-1);
      int recur = (recurValue!=null) ? ((Integer)recurValue) : 0;
      
      return (weekDay & recur) != 0 ;
    }
    
    public String toString() {
      return String.valueOf(recurValue);
    }
  }
  
  static final class MONTHLY extends Recurrence {
    public MONTHLY() {
      recurType = MONTHLY;
    }
    
    public String getName() {
      return "MONTHLY";
    }
    
    public void setRecurValue(Object recurVal) throws STDException {
      if (recurVal!=null && recurVal instanceof Long) {
        Long val = (Long)recurVal;
        if (val<1 || val>0x7FFFFFFF) {  //max to 31 bits
          throw new STDException("Recurrence value out of valid range: "+Long.toHexString(val));
        } else {
          Logger.d("Setting "+getName()+ " RecurValue to: " + Long.toHexString(val));
          this.recurValue = recurVal; 
        }
      } else {
        throw new STDException("Invalid recurrence value");
      }
    }

    public boolean isOccurringToday() {
      if (recurValue == null) return false;

      boolean result = false;
      Calendar c = Calendar.getInstance();
      long monDay = c.get(Calendar.DAY_OF_MONTH);
      
      if (recurValue instanceof int []) {
        for (int date : (int[]) recurValue) {
          if (monDay == date) {
            result = true;
            break;
          }
        }
      } else if (recurValue instanceof Long) {
        monDay = 1 << (monDay-1);
        long recur = (recurValue!=null) ? ((Long)recurValue) : 0;
        result =  (monDay & recur) != 0 ;
      }
      
      return result;
    }
    
    public String toString() {
      return String.valueOf(recurValue);
    }
  }
}
