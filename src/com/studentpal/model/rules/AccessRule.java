package com.studentpal.model.rules;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.studentpal.app.ResourceManager;
import com.studentpal.app.RuleScheduler;
import com.studentpal.app.RuleScheduler.RuleExecutor;
import com.studentpal.model.AccessCategory;
import com.studentpal.model.exception.STDException;
import com.studentpal.util.logger.Logger;

public class AccessRule {
  private static final String TAG = "@@ AccessRule";
  
  /*
   * Constants
   */
  public static final int ACCESS_DENIED = 0x01;
  public static final int ACCESS_PERMITTED = 0x02;
  
  /*
   * Member fields
   */
  private int access_type = ACCESS_DENIED;  
  
  private Recurrence _recurrence;
  private AccessCategory _adhereCate;
  private RuleExecutor  _ruleExecutor;
  private List<TimeRange> _timeRangeList;  
  
  public boolean isOccurringToday() {
    return _recurrence!=null && _recurrence.isOccurringToday();
  }
  
  public AccessCategory getAdhereCategory() {
    return _adhereCate;
  }
  
  public int getActionInTimeRange() {
    return access_type; 
  }
  
  public int getActionOutofTimeRange() {
    return access_type==ACCESS_DENIED ? ACCESS_PERMITTED : ACCESS_DENIED;
  }
  
  public void setRuleExcutor(RuleExecutor executor) {
    this._ruleExecutor = executor;
  }
  public RuleExecutor getRuleExcutor() {
    return this._ruleExecutor;
  }
  
  public void setRecurrence(Recurrence recur) {
    this._recurrence = recur;
  }
  
  public void addTimeRange(TimeRange range) {
    if (range == null || range.isValid()==false) {
      Logger.w(TAG, "Timerange is NULL or NOT valid!");
      return;
    }
    
    if (_timeRangeList == null) {
      _timeRangeList = new ArrayList<TimeRange>();  
    }
    _timeRangeList.add(range);
  }
  
  public List<TimeRange> getTimeRangeList() {
    return _timeRangeList;
  }
  
  /*
   * Inner class
   */
  public class ScheduledTime {
    int _hour; 
    int _minute;
    String _name = "";

    public ScheduledTime(String name) {
      this._name = name;
    }
    
    public String getName() {
      return _name;
    }
    
    public boolean isBeforeEqualTo(int hour, int minute) {
      boolean result = false;
      if (_hour<hour || (_hour==hour && _minute<=minute)) {
        result = true;
      }
      return result;
    }
    
    public boolean isAfter(int hour, int minute) {
      boolean result = false;
      if (_hour>hour || (_hour==hour && _minute>minute)) {
        result = true;
      }
      return result;
    }
    
    /*
     * 计算距离特定时间点的seconds数目
     * = 0: 本scheduled time正好等于指定的特定时间点
     * > 0: 本scheduled time在指定的特定时间点之前(尚未到达特定时间点)
     * < 0: 本scheduled time在指定的特定时间点之后(已经超过特定时间点) 
     */
    public int calcSecondsToSpecificTime(int hour, int minute, int second) {
      int seconds = ((hour-_hour)*60 + (minute-_minute)) * 60 - second;
      return seconds;
    }
  }
  
  
  public class TimeRange {
    private ScheduledTime startTime, endTime;
    
    public void setStartTime(int hour, int minute) throws STDException {
      if (startTime == null) {
        startTime = new ScheduledTime(ResourceManager.RES_STR_START_TIME);
      }
      setTime(startTime, hour, minute);
    }
    
    public void setEndTime(int hour, int minute) throws STDException {
      if (endTime == null) {
        endTime = new ScheduledTime(ResourceManager.RES_STR_END_TIME);
      }
      setTime(endTime, hour, minute);
    }
    
    public boolean isValid() {
      boolean result = true;
      if (startTime == null || endTime == null
          || startTime.isAfter(endTime._hour, endTime._minute)) {
        result = false;
      }
      return result;
    }
    ////////////////////////////////////////////////////////////////////////////
    private void setTime(ScheduledTime time, int hour, int minute)
        throws STDException {
      if ((hour > 23 && hour < 0) || (minute > 59 && minute < 0)) {
        String msg = "Invalid input time for " + time.getName() + "on HOUR: "
            + hour + "\tMINUTE: " + minute;
        Logger.w(TAG, msg);
        throw new STDException(msg);
      }
      time._hour = hour;
      time._minute = minute;
    }
  }
  
}
