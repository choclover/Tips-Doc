package com.studentpal.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;

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
  private static final boolean forTest = true;

  private static final int MONITORTASK_INTERVAL = 2000;  //mill-seconds
  private static final int MAX_WATCHED_APP_NUMBER = 64;
  
  /*
   * Field members
   */
  private static AccessController instance = null;
  private ClientEngine  engine = null;
  private ActivityManager activityManager = null;
  
  private Timer     _monitorTimer = null;
  private TimerTask _monitorTask  = null;
  
  private List<AccessCategory> _accessCategoryList;
  private HashMap<String, String> _restrictedAppsMap; 
  private Set<String> _processInKillingSet;
  
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
    
    if (_processInKillingSet != null) {
      _processInKillingSet.clear();
    } else {
      _processInKillingSet = new HashSet<String>();
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
    //runMonitoring(_restrictedAppsMap.size()>0 ? true : false);
    
    _loadAccessCategories(_accessCategoryList);
    for (AccessCategory accessCate : _accessCategoryList) {
      List<AccessRule> rules = accessCate.getAccessRules();
      RuleScheduler scheduler = accessCate.getScheduler();
      scheduler.reScheduleRules(rules); 
    }
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
      //First, kill restricted processes that is already running
      killRestrictedProcs();
      
      //we cannot reuse the old Timer if it is ever cancelled, so have to recreate one
      _monitorTimer = new Timer();
      if (_monitorTask == null) {
        _monitorTask = getMonitorTask();
      } 
      _monitorTimer.schedule(_monitorTask, 0, MONITORTASK_INTERVAL);
      
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
  
  public void appendRestrictedApp(ClientAppInfo appInfo) {
    ArrayList<ClientAppInfo> appList = new ArrayList<ClientAppInfo>(1);
    appendRestrictedAppList(appList);
  }  
  
  public void appendRestrictedAppList(List<ClientAppInfo> appList) {
    boolean append = true;
    _setRestrictedAppList(this._restrictedAppsMap, appList, append);
  }  
  
  public void setRestrictedAppList(List<ClientAppInfo> appList) {
    boolean append = false;
    _setRestrictedAppList(this._restrictedAppsMap, appList, append);
  }
  
  public void removeRestrictedAppList(List<ClientAppInfo> appList) {
    if (appList==null || appList.size()==0) return;
    
    synchronized(_restrictedAppsMap) {
      for (ClientAppInfo appInfo : appList) {
        _restrictedAppsMap.remove(appInfo.getIndexingKey());
      }
    }
    runMonitoring(_restrictedAppsMap.size()>0 ? true : false);
  }
  
  private void killRestrictedProcs() {
    List<RunningAppProcessInfo> processes = activityManager
        .getRunningAppProcesses();
    
    synchronized (_restrictedAppsMap) {
      for (RunningAppProcessInfo process : processes) {
        String pname = process.processName;
        if (_restrictedAppsMap.containsKey(pname)
            //&& process.pkgList.equals(restrictedAppsMap.get(pname))
        ) {
          Logger.i(TAG, "Ready to kill process: " + pname);
          Logger.i(TAG, "Ready to kill process: " + process);
          
          killProcess(process);
        }
      }
    }
  }
  public void killRestrictedApps() {
    List runningApps = null;
    boolean useProcessInfo = true;
    
    if (useProcessInfo) {
      runningApps = activityManager.getRunningAppProcesses();
    } else {
      runningApps = activityManager.getRunningTasks(MAX_WATCHED_APP_NUMBER);
      //List<RecentTaskInfo> recentTasks = activityManager.getRecentTasks(MAX_WATCHED_APP_NUMBER, 0);
    }
    
    synchronized (_restrictedAppsMap) {
      boolean restrictedAppFound = false;
        
      for (Object app : runningApps) {
        String pkgName = null;
        String clzName = null;
        if (app instanceof RunningAppProcessInfo) {
          pkgName = ((RunningAppProcessInfo)app).processName;
          
        } else if (app instanceof RunningTaskInfo) {
          pkgName = ((RunningTaskInfo)app).baseActivity.getPackageName();
          clzName = ((RunningTaskInfo)app).baseActivity.getClassName();
        }
        
        if (_restrictedAppsMap.containsKey(pkgName)
            //&& _restrictedAppsMap.get(pkgName).equals(clzName)
        ) {
          RunningAppProcessInfo appProc;
          
          if (app instanceof RunningAppProcessInfo) {
            appProc = (RunningAppProcessInfo)app;
            
          } else /*if (app instanceof RunningTaskInfo)*/ {
            appProc = findRunningAppProcess(pkgName);
            if (appProc == null) {
              Logger.w(TAG, "Cannot find process for task " + pkgName);
              continue;
            }
          }

          Logger.i(TAG, "Ready to kill application: " + pkgName);
          Logger.i(TAG, "Ready to kill application: " + app);
          
          killProcess(appProc);
          restrictedAppFound = true;

          if (_processInKillingSet.contains(pkgName)) {
            Logger.d(TAG, pkgName+" is being killed, skipping...");
            continue;
          } else {
            _processInKillingSet.add(pkgName);
          }
          
          this.engine.launchNewActivity(AccessDeniedNotification.class);
//          this.engine.showAccessDeniedNotification();
//          engine.getMsgHandler().sendEmptyMessage(
//              Event.SIGNAL_SHOW_ACCESS_DENIED_NOTIFICATION);
        }
      }
      
      //Not found any application that is restricted
      if (! restrictedAppFound) {
        //Logger.d(TAG, "clearing _processInKillingSet...");
        _processInKillingSet.clear();
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
      appInfo = new ClientAppInfo("Messaging", "com.android.mms",
          "com.android.mms.Messaging");
      appList.add(appInfo);
      appInfo = new ClientAppInfo("Alarmclock", "com.android.alarmclock",
          "com.android.alarmclock.Alarmclock");
      appList.add(appInfo);
      appInfo = new ClientAppInfo("Browser", "com.android.browser",
          "com.android.browser.Browser");
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
          //restrictedAppsMap.put(appInfo.getAppPkgname(), appInfo.getAppClassname());
          restrictedAppsMap.put(appInfo.getIndexingKey(), appInfo.getIndexingValue());
        }
      }
      
      runMonitoring(_restrictedAppsMap.size()>0 ? true : false);
    }
  }
  
  private TimerTask getMonitorTask() {
    TimerTask task = new TimerTask() {
      public void run() {
        Logger.v(TAG, "Monitor Task starts to run!");
        killRestrictedApps();
      }
    };
    return task;
  }
  
  private AccessCategory getDailyCate() {
    AccessCategory aCate = new AccessCategory();
    aCate.set_id(1);
    aCate.set_name("Cate 1");      
    aCate.addManagedApp(new ClientAppInfo("Messaging", "com.android.mms",
        "com.android.mms.Messaging"));
    aCate.addManagedApp(new ClientAppInfo("Alarmclock",
        "com.android.alarmclock", "com.android.alarmclock.Alarmclock"));
    aCate.addManagedApp(new ClientAppInfo("Browser", "com.android.browser",
        "com.android.browser.Browser"));
    
    try {
      AccessRule aRule = new AccessRule();

      Recurrence recur = Recurrence.getInstance(Recurrence.DAILY);
      aRule.setRecurrence(recur);
      aRule.setAccessType(AccessRule.ACCESS_DENIED);

      TimeRange tr = null;

      tr = new TimeRange();
      tr.setStartTime(8, 22);
      tr.setEndTime(8, 23);
      aRule.addTimeRange(tr);

      tr = new TimeRange();
      tr.setStartTime(11, 26);
      tr.setEndTime(11, 27);
      aRule.addTimeRange(tr);

      tr = new TimeRange();
      tr.setStartTime(11, 28);
      tr.setEndTime(11, 30);
      aRule.addTimeRange(tr);

      aCate.addAccessRule(aRule);
      
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
  
  private RunningAppProcessInfo findRunningAppProcess(String classname) {
    RunningAppProcessInfo result = null;
    
    List<RunningAppProcessInfo> processes = activityManager
        .getRunningAppProcesses();
    for (RunningAppProcessInfo process : processes) {
      String pname = process.processName;
      if (classname.equals(pname)) {
        result = process;
        break;
      }
    }
    return result;
  }

  private boolean killProcess(RunningAppProcessInfo p) {
    int apiVer = android.os.Build.VERSION.SDK_INT;
    String[] pkgs = p.pkgList;
    for (String pkg : pkgs) {
      if (apiVer <= android.os.Build.VERSION_CODES.ECLAIR_MR1) {
        //for API 2.1 and earlier version
        activityManager.restartPackage(pkg);
      } else if (apiVer >= android.os.Build.VERSION_CODES.FROYO) {
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
