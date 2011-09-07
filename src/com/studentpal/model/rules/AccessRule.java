package com.studentpal.model.rules;

import java.util.Calendar;
import java.util.Date;

import com.studentpal.model.AccessCategory;
import com.studentpal.util.logger.Logger;

public class AccessRule {
  private static final String TAG = "@@ AccessRule";
  
  /*
   * Constants
   */
  static final int ACCESS_DENIED = 0x01;
  static final int ACCESS_PERMITTED = 0x02;
  
  /*
   * Inner class
   */
  class ScheduledTime {
    int _hour; 
    int _minute;
    
    public boolean priorTo(int hour, int minute) {
      boolean result = false;
      if (_hour<hour || (_hour==hour && _minute<minute)) {
        result = true;
      }
      return result;
    }
  }
  
  /*
   * Member fields
   */
  private int access_type = ACCESS_DENIED;
  private Recurrence recurrence;
  private ScheduledTime startTime, endTime;
  private AccessCategory _adhereCate;
  
  public void setStartTime(int hour, int minute) {
    setTime(startTime, hour, minute);
  }
  public void setEndTime(int hour, int minute) {
    setTime(endTime, hour, minute);
  }
  
  public void exeucte() {
    if (recurrence==null || false==recurrence.isOccurringToday()) {
      return;
    }
    
    final Calendar c = Calendar.getInstance(); 
    int mHour = c.get(Calendar.HOUR_OF_DAY);
    int mMinute = c.get(Calendar.MINUTE);
    if (endTime.priorTo(mHour, mMinute)) {
      Logger.i(TAG, "Endtime has passed!");
    }
  }
  
  public AccessCategory getAdhereCate() {
    return _adhereCate;
  }
  //////////////////////////////////////////////////////////////////////////////
  private void setTime(ScheduledTime time, int hour, int minute) {
    time._hour = hour;
    time._minute = minute;
  }
  
  
}
