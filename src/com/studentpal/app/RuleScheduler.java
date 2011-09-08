package com.studentpal.app;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.studentpal.engine.AppHandler;
import com.studentpal.engine.ClientEngine;
import com.studentpal.model.AccessCategory;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.model.rules.AccessRule;
import com.studentpal.util.logger.Logger;

public class RuleScheduler implements AppHandler {
  private static final String TAG = "@@ RuleScheduler";

  /*
   * Field members
   */
  private ScheduledExecutorService inner_scheduler;
  private List<ScheduledFuture> _pendingTaskList;

  public RuleScheduler() {
    _pendingTaskList = new ArrayList<ScheduledFuture>();
  }

  public void reScheduleRules(List<AccessRule> rules) {
    boolean reschedule = true;
    scheduleRules(rules, reschedule);
  }

  public void scheduleRules(List<AccessRule> rules, boolean bRescheduleAll) {
    if (rules == null || rules.size() == 0)
      return;

    if (inner_scheduler == null) {
      inner_scheduler = Executors.newScheduledThreadPool(rules.size());
    }

    if (bRescheduleAll) {
      terminateAllPendingTasks();
      inner_scheduler = null;
      inner_scheduler = Executors.newScheduledThreadPool(rules.size());
    }

    for (AccessRule aRule : rules) {
      RuleExecutor executor = createRuleExecutor(aRule);
      if (executor != null) {
        ScheduledFuture pendingTask = inner_scheduler.schedule(executor,
            executor.delay, SECONDS);
        _pendingTaskList.add(pendingTask);
      }
    }
  }

  public RuleExecutor createRuleExecutor(AccessRule aRule) {
    RuleExecutor result = null;

    if (aRule != null && aRule.isOccurringToday()) {
      final Calendar now = Calendar.getInstance();
      int nowHour = now.get(Calendar.HOUR_OF_DAY);
      int nowMin = now.get(Calendar.MINUTE);
      int nowSec = now.get(Calendar.SECOND);

      int delay = 0;
      if ((delay = aRule.getEndTime().calcSecondsToSpecificTime(nowHour,
          nowMin, nowSec)) >= 0) {
        // endtime is before or equal to now time
        // Do nothing
        Logger.i(TAG, "Endtime has passed and is before or equal to now time!");

      } else if ((delay = aRule.getStartTime().calcSecondsToSpecificTime(
          nowHour, nowMin, nowSec)) >= 0) {
        // starttime is before or eqal to now time
        Logger.i(TAG, "Starttime is before or eqal to now time!");
        result = new RuleExecutor(aRule);
        result.setAction(aRule.getActionInTimeRange());
        result.setDelayTime(delay);

      } else /*
              * if ((delay = aRule.getStartTime().calcSecondsToSpecificTime(
              * nowHour, nowMin, nowSec)) < 0)
              */
      {
        // starttime is after now time
        Logger.i(TAG, "Starttime is after now time!");
        result = new RuleExecutor(aRule);
        result.setAction(aRule.getActionOutofTimeRange());
        result.setDelayTime(delay);
      }
    }

    aRule.setRuleExcutor(result);

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
    for (ScheduledFuture<?> pendingTask : _pendingTaskList) {
      if (pendingTask != null) {
        pendingTask.cancel(false); // will not interrupt the task if it has
                                   // started to run
      }
    }
    _pendingTaskList.clear();
  }

  /*
   * Inner class
   */
  public class RuleExecutor implements Runnable {
    int delay = 0; // delay in seconds
    int action = 0;
    private AccessRule _adhereRule;

    public RuleExecutor(AccessRule rule) {
      _adhereRule = rule;
    }

    // public void setAccessRule (AccessRule rule) {
    // _adhereRule = rule;
    // }

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

      ArrayList<ClientAppInfo> accessPermittedAppList = new ArrayList<ClientAppInfo>();

      AccessCategory accCate = _adhereRule.getAdhereCategory();
      Set<ClientAppInfo> appInfoSet = accCate.getManagedApps().keySet();
      for (ClientAppInfo appInfo : appInfoSet) {
        accCate.adjustRestrictedRuleCount(appInfo, denyCntDelta);

        if (accCate.isAccessPermitted(appInfo)) {
          accessPermittedAppList.add(appInfo);
        }
      }

      if (accessPermittedAppList.size() > 0) {
        ClientEngine.getInstance().getAccessController()
            .removeRestrictedAppList(accessPermittedAppList);
      }
    }

    public void setAction(int action) {
      this.action = action;
    }

    public void setDelayTime(int seconds) {
      delay = seconds;
    }

  }

}
