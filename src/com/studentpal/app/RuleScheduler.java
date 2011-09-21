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
  private List<RuleExecutor> _ruleExecutorsList;
  // private List<ScheduledFuture> _pendingTaskList;

  private static final Comparator<ScheduledTime> timePointComparator = new Comparator<ScheduledTime>() {
    @Override
    // sort by ascending order
    public int compare(ScheduledTime t1, ScheduledTime t2) {
      int result = t2.calcSecondsToSpecificTime(t1._hour, t1._minute, 0);
      return result;
    }
  };

  public RuleScheduler() {
    // _pendingTaskList = new ArrayList<ScheduledFuture>();
    _ruleExecutorsList = new ArrayList<RuleExecutor>();
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
      _ruleExecutorsList.clear();

      inner_scheduler = null;
      inner_scheduler = Executors.newScheduledThreadPool(rules.size());
    }

    for (AccessRule aRule : rules) {
      if (aRule == null) {
        Logger.w(TAG, "Rule should NOT be NULL!");
      } else if (aRule.isOccurringToday() == false) {
        Logger.i(TAG, "Rule " +aRule.getRecurrence().getName()+
            " is NOT occurring today, skipping it...");
        continue;
      }
      
      RuleExecutor executor = createRuleExecutor(aRule);
      if (executor != null) {
        _ruleExecutorsList.add(executor);

        int currAction = executor.getCurrentAction();
        executor.updateRestrictedAppsMapByAction(currAction);
        executor.scheduleNextTask();
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

  // ////////////////////////////////////////////////////////////////////////////
  private void terminateAllPendingTasks() {
    for (RuleExecutor executor : _ruleExecutorsList) {
      executor.terminate();
    }
    _ruleExecutorsList.clear();
  }

  // ////////////////////////////////////////////////////////////////////////////
  /*
   * Inner class
   */
  public class RuleExecutor implements Runnable {
   
    int futureAction = 0;
    private AccessRule _adhereRule;
    private SortedSet<ScheduledTime> timePointsSet;
    private ScheduledFuture<?> pendingTask;

    public RuleExecutor(AccessRule rule) {
      _adhereRule = rule;
      
      if (timePointsSet == null) {
        this.timePointsSet = new TreeSet<ScheduledTime>(timePointComparator);
        populateTimePointsSet(this.timePointsSet);
      }
    }

    public void setAccessRule(AccessRule rule) {
      _adhereRule = rule;
    }

    public ScheduledFuture scheduleNextTask() {
      ScheduledFuture task = null;

      if (timePointsSet.size() > 0) {
        int delay = 0; // delay in seconds
        
        final Calendar now = Calendar.getInstance();
        int nowHour = now.get(Calendar.HOUR_OF_DAY);
        int nowMin = now.get(Calendar.MINUTE);
        int nowSec = now.get(Calendar.SECOND);
        Logger.i(TAG, "Now is: " + nowHour + ':' + nowMin + ':' + nowSec);

        // 为最近的时间点设置一个定时执行器
        for (ScheduledTime timePoint : timePointsSet) {
          delay = timePoint.calcSecondsToSpecificTime(nowHour, nowMin, nowSec);

          if (delay < 0) { // 当前时间还未（或者刚刚）到达timePoint
            if (timePoint.isStartTime()) {
              this.futureAction = _adhereRule.getActionInTimeRange();
              
            } else {
              this.futureAction = _adhereRule.getActionOutofTimeRange();
            }

            StringBuffer msg = new StringBuffer().append("Scheduled time(")
                .append(timePoint.toIntValue()).append(") to now(").append(
                    nowHour).append(':').append(nowMin).append(':').append(
                    nowSec).append(") is ").append(delay).append(
                    " seconds in action ").append(futureAction);
            Logger.d(TAG, msg.toString());

            task = inner_scheduler.schedule(this, Math.abs(delay), SECONDS);
            break;  //only need to schedule the first task
          }
        }//for

        // 当前时间超过了所有的timePoint
        if (delay > 0) {
          Logger.i(TAG, "Last Endtime has passed! -- delay:"+delay);
        }
        
      } //if

      this.pendingTask = task;
      return task;
    }

    public int getCurrentAction() {
      // default is OutofTimeRange action
      int currAction = _adhereRule.getActionOutofTimeRange();

      if (timePointsSet.size() > 0) {
        final Calendar now = Calendar.getInstance();
        int nowHour = now.get(Calendar.HOUR_OF_DAY);
        int nowMin = now.get(Calendar.MINUTE);
        int nowSec = now.get(Calendar.SECOND);
        Logger.i(TAG, "Now is: " + nowHour + ':' + nowMin + ':' + nowSec);

        // 为最近的时间点设置一个定时执行器
        for (ScheduledTime timePoint : timePointsSet) {
          int delay = timePoint.calcSecondsToSpecificTime(nowHour, nowMin,
              nowSec);

          if (delay <= 0) { // 当前时间还未（或者刚刚）到达timePoint
            if (timePoint.isStartTime()) {
              currAction = _adhereRule.getActionOutofTimeRange(); // 在startTime之前，则是OutofTimeRange
            } else {
              currAction = _adhereRule.getActionInTimeRange();
            }

            StringBuffer msg = new StringBuffer().append(
                "Action on current time(").append(nowHour).append(':').append(
                nowMin).append(':').append(nowSec).append(") is: ").append(
                currAction);
            Logger.d(TAG, msg.toString());
            break; // only need to schedule the first task
          }
        }// for
      }

      return currAction;
    }
    
    @Override
    public void run() {
      Logger.i(TAG, "Task start to run with action: " + futureAction);

      updateRestrictedAppsMapByAction(this.futureAction);

      // 为下一个时间点设置定时器
      scheduleNextTask();
    }

    public void terminate() {
      if (this.pendingTask != null) {
        pendingTask.cancel(true);
      }
      if (this.timePointsSet != null) {
        timePointsSet.clear();
        timePointsSet = null;
      }
    }

    // //////////////////////////////////////////////////////////////////////////
    // private void setPendingTask(ScheduledFuture task) {
    // this.pendingTask = task;
    // }

    private void populateTimePointsSet(SortedSet<ScheduledTime> timePointsSet) {
      List<TimeRange> timeRangeList = _adhereRule.getTimeRangeList();

      // 每次scheduleNextTask()都生成有序的时间点的列表
      for (TimeRange timeRange : timeRangeList) {
        ScheduledTime startTime = timeRange.getStartTime();
        ScheduledTime endTime = timeRange.getEndTime();

        if (timePointsSet.contains(startTime) || timePointsSet.contains(endTime)) {
          Logger.w(TAG, "Start time(" + startTime.toIntValue()
              + ") or End time(" + endTime.toIntValue()
              + ") is overlapped with others!");
        } else {
          timePointsSet.add(startTime);
          timePointsSet.add(endTime);
        }
      }// for
      
      for (ScheduledTime timePoint : timePointsSet) {
        Logger.d(TAG, "ASC TimePoint is: "+timePoint._hour+":"+timePoint._minute);
      }
    }
    
    private void updateRestrictedAppsMapByAction(int actionType) {
      Logger.v(TAG, "Enter updateRestrictedAppsMapByAction in action "+actionType);
      
      int denyCntDelta = 0;
      switch (actionType) {
      case AccessRule.ACCESS_DENIED:
        denyCntDelta = 1;
        break;
      case AccessRule.ACCESS_PERMITTED:
        denyCntDelta = -1;
        break;
      default:
        Logger.w(TAG, "Invalid action type of "+actionType);
        break;
      }

      List<ClientAppInfo> restrictedAppList = new ArrayList<ClientAppInfo>();
      List<ClientAppInfo> permittedAppList = new ArrayList<ClientAppInfo>();
      
      AccessCategory accCate = _adhereRule.getAdhereCategory();
      Set<ClientAppInfo> appInfoSet = accCate.getManagedApps().keySet();
      for (ClientAppInfo appInfo : appInfoSet) {
        if (denyCntDelta != 0) {
          accCate.adjustRestrictedRuleCount(appInfo, denyCntDelta);
        }

        if (true == accCate.isAccessDenied(appInfo)) {
          restrictedAppList.add(appInfo);
        } else {
          permittedAppList.add(appInfo);
        }
        
      }// for

      if (restrictedAppList.size() > 0)
      {
        //ClientEngine.getInstance().getAccessController().setRestrictedAppList(
        ClientEngine.getInstance().getAccessController().appendRestrictedAppList(
            restrictedAppList);
      }
      if (permittedAppList.size() > 0)
      {
        ClientEngine.getInstance().getAccessController().removeRestrictedAppList(
            permittedAppList);
      }
    }

  }// class RuleExecutor

}
