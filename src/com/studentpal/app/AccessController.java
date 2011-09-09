package com.studentpal.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;

import com.studentpal.engine.AppHandler;
import com.studentpal.engine.ClientEngine;
import com.studentpal.model.AccessCategory;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.model.exception.STDException;
import com.studentpal.model.rules.AccessRule;
import com.studentpal.model.rules.Recurrence;
import com.studentpal.model.rules.TimeRange;
import com.studentpal.ui.AccessDeniedNotification;
import com.studentpal.util.logger.Logger;


public class AccessController implements AppHandler {
  /*
   * Field constants
   */
  private static final String TAG = "@@ AccessController";
  private static final int MONITORTASK_PERIOD = 2000;  //mill-seconds
  private static final boolean forTest = true;
  
  /*
   * Field members
   */
  private static AccessController instance = null;
  private ClientEngine  engine = null;
  private ActivityManager activityManager = null;
//  private RuleScheduler ruleScheduler = null;
  
  private Timer     _monitorTimer = null;
  private TimerTask _monitorTask  = null;
  
  private HashMap<String, String> _restrictedAppsMap; 
  private List<AccessCategory> _accessCategoryList;
  
  /*
   * Methods
   */
  private AccessController() {
  }
  
  public static AccessController getInstance() {
    if (instance == null) {
      instance = new AccessController();
    }
    return instance;
  }

  private void initialize() {
    this.engine = ClientEngine.getInstance();
    this.activityManager = engine.getActivityManager();

    if (_restrictedAppsMap != null) {
      _restrictedAppsMap.clear();
    } else {
      _restrictedAppsMap = new HashMap<String, String>();
    }
    
    if (_accessCategoryList != null) {
      _accessCategoryList.clear();
    } else {
      _accessCategoryList = new ArrayList<AccessCategory>();
    }
    
  }
  
  @Override
  public void launch() {
    initialize();
    
    _loadRestrictedApps(_restrictedAppsMap);

    _loadAccessCategories(_accessCategoryList);
    for (AccessCategory accessCate : _accessCategoryList) {
      List<AccessRule> rules = accessCate.getAccessRules();
      RuleScheduler scheduler = accessCate.getScheduler();
      scheduler.reScheduleRules(rules); 
    }

    runMonitoring(_restrictedAppsMap.size()>0 ? true : false);
    
  }

  @Override
  public void terminate() {
    if (_restrictedAppsMap != null) {
      _restrictedAppsMap.clear();
    }
    runMonitoring(false);
    
    if (_accessCategoryList != null) {
      for (AccessCategory accessCate : _accessCategoryList) {
        RuleScheduler scheduler = accessCate.getScheduler();
        if (scheduler != null) {
          scheduler.terminate();
        }
      }
    }
  }
  
  public void runMonitoring(boolean runMonitor) {
    Logger.i(TAG, "Ready to run monitoring is: "+runMonitor);
    
    if (runMonitor==true) {
      //we cannot reuse the old Timer if it is ever cancelled, so have to recreate one
      _monitorTimer = new Timer();
      if (_monitorTask == null) {
        _monitorTask = getMonitorTask();
      } 
      _monitorTimer.schedule(_monitorTask, 0, MONITORTASK_PERIOD);
      
    } else {
      if (_monitorTask != null) {
        _monitorTask.cancel();
        _monitorTask = null;
      }
      if (_monitorTimer != null) {
        _monitorTimer.purge();
        _monitorTimer.cancel();
        _monitorTimer = null;
      }
    }
  }
  
  public void setRestrictedAppList(List<ClientAppInfo> appList) {
    boolean append = false;
    _setRestrictedAppList(this._restrictedAppsMap, appList, append);
  }

  public void appendRestrictedAppList(List<ClientAppInfo> appList) {
    boolean append = true;
    _setRestrictedAppList(this._restrictedAppsMap, appList, append);
  }
  
  public void appendRestrictedApp(ClientAppInfo appInfo) {
    ArrayList<ClientAppInfo> appList = new ArrayList<ClientAppInfo>(1);
    boolean append = true;
    _setRestrictedAppList(this._restrictedAppsMap, appList, append);
  }
  
  public void removeRestrictedAppList(List<ClientAppInfo> appList) {
    if (appList==null || appList.size()==0) return;
    
    for (ClientAppInfo appInfo : appList) {
      synchronized(_restrictedAppsMap) {
        _restrictedAppsMap.remove(appInfo.getAppClassname());
      }
    }
  }
  
  public void killRestrictedApps() {
    List<RunningAppProcessInfo> processes = activityManager
        .getRunningAppProcesses();

    synchronized (_restrictedAppsMap) {
      for (RunningAppProcessInfo process : processes) {
        String pname = process.processName;
        if (_restrictedAppsMap.containsKey(pname)
            //&& process.pkgList.equals(restrictedAppsMap.get(pname))
        ) {
          Logger.i(TAG, "Ready to kill application: " + pname);
          
          killProcess(process);
          this.engine.launchActivity(AccessDeniedNotification.class);
        }
      }
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////
  private void _loadAccessCategories(List intoList) {
    //TODO read access categories from config
    if (forTest) {
      intoList.add(this.getDailyCate());
    }
  }
  
  private void _loadRestrictedApps(HashMap intoMap) {
    //TODO read restricted app list from config
    List<ClientAppInfo> appList = null;
    if (forTest) {
      appList = new ArrayList<ClientAppInfo>();
      ClientAppInfo appInfo = null;
      appInfo = new ClientAppInfo("Messaging", "com.android.mms", null);
      appList.add(appInfo);
      appInfo = new ClientAppInfo("Alarmclock", "com.android.alarmclock", null);
      appList.add(appInfo);
      appInfo = new ClientAppInfo("Browser", "com.android.browser", null);
      appList.add(appInfo);
    }
    
    boolean append = false;
    _setRestrictedAppList(intoMap, appList, append);
  }
  
  private void _setRestrictedAppList(HashMap<String, String> restrictedAppsMap, 
      List<ClientAppInfo> appList, boolean append) {
    if (restrictedAppsMap == null) {
      Logger.w(TAG, "Input restrictedAppsMap should NOT be NULL!");
      return;
    }
    
    if (appList!=null && appList.size()>0) {
      synchronized(restrictedAppsMap) {
        if (!append) {
          restrictedAppsMap.clear();
        }
        for (ClientAppInfo appInfo : appList) {
          if (appInfo == null) continue;
          //restrictedAppsMap.put(appInfo.getAppName(), appInfo.getAppClassname());
          restrictedAppsMap.put(appInfo.getAppClassname(), appInfo.getAppClassname());
        }
      }
    }
  }
  
  private TimerTask getMonitorTask() {
    TimerTask task = new TimerTask() {
      public void run() {
        Logger.d(TAG, "Monitor Task starts to run!");
        killRestrictedApps();
      }
    };
    return task;
  }
  
  private AccessCategory getDailyCate() {
    AccessCategory aCate = new AccessCategory();
    try {
      AccessRule aRule = new AccessRule();

      Recurrence recur = Recurrence.getInstance(Recurrence.DAILY);
      aRule.setRecurrence(recur);

      TimeRange tr = null;

      tr = new TimeRange();
      tr.setStartTime(7, 30);
      tr.setEndTime(8, 30);
      aRule.addTimeRange(tr);

      tr = new TimeRange();
      tr.setStartTime(9, 30);
      tr.setEndTime(10, 30);
      aRule.addTimeRange(tr);

      tr = new TimeRange();
      tr.setStartTime(11, 30);
      tr.setEndTime(12, 30);
      aRule.addTimeRange(tr);

      aCate.set_id(1);
      aCate.set_name("Cate 1");
      aCate.addAccessRule(aRule);
      aCate.addManagedApp(new ClientAppInfo("Messaging", "com.android.mms",
          null));
      aCate.addManagedApp(new ClientAppInfo("Alarmclock",
          "com.android.alarmclock", null));
      aCate.addManagedApp(new ClientAppInfo("Browser", "com.android.browser",
          null));
    } catch (STDException e) {
      Logger.w(TAG, e.toString());
    }
    
    return aCate;
  }
  
  private AccessCategory getWeeklyCate() {
    AccessCategory aCate = new AccessCategory();
    return aCate;
  } 
   
  private AccessCategory getMonthlyCate() {
    AccessCategory aCate = new AccessCategory();
    return aCate;
  }
  
  
  private boolean killProcess(RunningAppProcessInfo p) {
    int apiVer = android.os.Build.VERSION.SDK_INT;
    String[] pkgs = p.pkgList;
    for (String pkg : pkgs) {
      if (apiVer <= android.os.Build.VERSION_CODES.ECLAIR_MR1) {
        //for API 2.1 and earlier version
        activityManager.restartPackage(pkg);
      } else if (apiVer > android.os.Build.VERSION_CODES.FROYO) {
        //for API 2.2 and higher version
        activityManager.killBackgroundProcesses(pkg);
      } else {
        android.os.Process.killProcess(p.pid);
        //android.os.Process.killProcess(android.os.Process.myPid());
      }
    }

    return true;
  }

}
