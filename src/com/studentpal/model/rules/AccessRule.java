package com.studentpal.model.rules;

public class AccessRule {
  /*
   * Constants
   */
  static final int ACCESS_DENIED = 0x01;
  static final int ACCESS_PERMITTED = 0x02;
  
  /*
   * Inner class
   */
  class ScheduledTime {
    int hour; 
    int minute;
  }
  
  /*
   * Member fields
   */
  private ScheduledTime startTime, endTime;
  
  
  public void setStartTime(int hour, int minute) {
    setTime(startTime, hour, minute);
  }
  public void setEndTime(int hour, int minute) {
    setTime(endTime, hour, minute);
  }
  
  //////////////////////////////////////////////////////////////////////////////
  private void setTime(ScheduledTime time, int hour, int minute) {
    time.hour = hour;
    time.minute = minute;
  }
}
