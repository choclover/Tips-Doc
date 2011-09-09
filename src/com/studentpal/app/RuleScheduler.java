package com.studentpal.app;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.studentpal.engine.AppHandler;
import com.studentpal.engine.ClientEngine;
import com.studentpal.model.AccessCategory;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.model.rules.AccessRule;
import com.studentpal.model.rules.TimeRange;
import com.studentpal.model.rules.TimeRange.ScheduledTime;
import com.studentpal.util.logger.Logger;

public class RuleScheduler implements AppHandler {
  private static final String TAG = "@@ RuleScheduler";

  /*
   * Field members
   */
  private ScheduledExecutorService inner_scheduler;
  private List<RuleExecutor> _rulesExecutorList;
//  private List<ScheduledFuture> _pendingTaskList;

  public RuleScheduler() {
//    _pendingTaskList = new ArrayList<ScheduledFuture>();
    _rulesExecutorList = new ArrayList<RuleExecutor>();
  }

  public void reScheduleRules(List<AccessRule> rules) {
    boolean reschedule = true;
    scheduleRules(rules, reschedule);
  }

  public void scheduleRules(List<AccessRule> rules, boolean bRescheduleAll) {
    if (rules == null || rules.size() == 0)
      return;

    if (bRescheduleAll || inner_scheduler == null) {
      terminateAllPendingTasks();
      _rulesExecutorList.clear();

      inner_scheduler = null;
      inner_scheduler = Executors.newScheduledThreadPool(rules.size());
    }

    for (AccessRule aRule : rules) {
      RuleExecutor executor = createRuleExecutor(aRule);
      if (executor != null) {
        _rulesExecutorList.add(executor);
        executor.scheduleNextTask(aRule);
      }
    }
  }

  RuleExecutor createRuleExecutor(AccessRule aRule) {
    RuleExecutor result = null;
    result = new RuleExecutor(aRule);
    return result;
  }

  public void terminate() {
    terminateAllPendingTasks();
  }

  @Override
  public void launch() {
  }

  //////////////////////////////////////////////////////////////////////////////
  private void terminateAllPendingTasks() {
    for (RuleExecutor executor : _rulesExecutorList) {
      executor.terminate();
    }
    _rulesExecutorList.clear();
  }

  //////////////////////////////////////////////////////////////////////////////
  /*
   * Inner class
   */
  public class RuleExecutor implements Runnable {
    int delay = 0; // delay in seconds
    int action = 0;
    private AccessRule _adhereRule;
    private ArrayList<ClientAppInfo> _accessPermittedAppList;
    private ScheduledFuture pendingTask;
    
    public RuleExecutor(AccessRule rule) {
      _adhereRule = rule;
      _accessPermittedAppList = new ArrayList<ClientAppInfo>();
    }

    public void setAccessRule(AccessRule rule) {
      _adhereRule = rule;
    }

    public ScheduledFuture scheduleNextTask(AccessRule rule) {
      ScheduledFuture task = null;
      
      List<TimeRange> timeRangeList = rule.getTimeRangeList();
      SortedSet<ScheduledTime> timePointSet = new TreeSet<ScheduledTime>(
          new Comparator<ScheduledTime>() {
            @Override
            public int compare(ScheduledTime t1, ScheduledTime t2) {
              int result = t1.calcSecondsToSpecificTime(t2._hour, t2._minute, 0);
              return result;  
            }
          });
      
      //生成有序的时间点的列表
      for (TimeRange timeRange : timeRangeList) {
        ScheduledTime startTime = timeRange.getStartTime();
        ScheduledTime endTime   = timeRange.getEndTime();
        
        if (timePointSet.contains(startTime) || timePointSet.contains(endTime)) {
          Logger.w(TAG, "Start time(" + startTime.toIntValue()
              + ") or End time(" + endTime.toIntValue()
              + ") is overlapped with others!");
        } else {
          timePointSet.add(startTime);
          timePointSet.add(endTime);
        }
      }
      
      if (timePointSet.size() > 0) {
        final Calendar now = Calendar.getInstance();
        int nowHour = now.get(Calendar.HOUR_OF_DAY);
        int nowMin = now.get(Calendar.MINUTE);
        int nowSec = now.get(Calendar.SECOND);
        
        //为最近的时间点设置一个定时执行器
        for (ScheduledTime timePoint : timePointSet) {
          delay = timePoint.calcSecondsToSpecificTime(nowHour, nowMin, nowSec);
          if (delay <= 0) {  //当前时间还未或者刚刚到达timePoint
            if (timePoint.isStartTime()) {
              this.action = _adhereRule.getActionOutofTimeRange();
            } else {
              this.action = _adhereRule.getActionInTimeRange();
            }
            
            task = inner_scheduler.schedule(this, Math.abs(delay), SECONDS);
            break;
          }
        }
        
        //当前时间超过了所有的timePoint
        if (delay > 0) {
          // Do nothing
          Logger.i(TAG, "Last Endtime has passed!");
        }
      }
      
      setPendingTask(task);
      return task;
    }
    
    @Override
    public void run() {
      int denyCntDelta = 0;
      switch (this.action) {
      case AccessRule.ACCESS_DENIED:
        denyCntDelta = 1;
        break;
      case AccessRule.ACCESS_PERMITTED:
        denyCntDelta = -1;
        break;
      default:
        break;
      }

      _accessPermittedAppList.clear();  //reset
      
      AccessCategory accCate = _adhereRule.getAdhereCategory();
      Set<ClientAppInfo> appInfoSet = accCate.getManagedApps().keySet();
      for (ClientAppInfo appInfo : appInfoSet) {
        accCate.adjustRestrictedRuleCount(appInfo, denyCntDelta);

        if (accCate.isAccessPermitted(appInfo)) {
          _accessPermittedAppList.add(appInfo);
        }
      }

      if (_accessPermittedAppList.size() > 0) {
        ClientEngine.getInstance().getAccessController()
            .removeRestrictedAppList(_accessPermittedAppList);
      }
      
      //为下一个时间点设置定时器
      scheduleNextTask(_adhereRule);
      
    }

//    public void setAction(int action) {
//      this.action = action;
//    }
//
//    public void setDelayTime(int seconds) {
//      delay = seconds;
//    }
    
    public void terminate() {
      if (this.pendingTask != null) {
        pendingTask.cancel(true);
      }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    private void setPendingTask(ScheduledFuture task) {
      this.pendingTask = task;
    }
  }

}
