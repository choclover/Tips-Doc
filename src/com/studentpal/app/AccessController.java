package com.studentpal.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.studentpal.engine.AppHandler;
import com.studentpal.engine.ClientEngine;
import com.studentpal.model.AccessCategory;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.model.rules.AccessRule;
import com.studentpal.ui.AccessDeniedNotification;
import com.studentpal.ui.AccessRequestForm;
import com.studentpal.util.logger.Logger;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Intent;


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
  
  private Timer     monitorTimer = null;
  private TimerTask monitorTask  = null;
  
  private HashMap<String, String> restrictedAppsMap; 
  private List<AccessCategory> accessCategoryList;
  
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

    if (restrictedAppsMap != null) {
      restrictedAppsMap.clear();
    } else {
      restrictedAppsMap = new HashMap<String, String>();
    }
    
    if (accessCategoryList != null) {
      accessCategoryList.clear();
    } else {
      accessCategoryList = new ArrayList<AccessCategory>();
    }
    
  }
  
  @Override
  public void launch() {
    initialize();
    
    _loadRestrictedApps(restrictedAppsMap);

    _loadAccessCategories(accessCategoryList);
    for (AccessCategory accessCate : accessCategoryList) {
      List<AccessRule> rules = accessCate.getAccessRules();
      RuleScheduler scheduler = accessCate.getScheduler();
      scheduler.reScheduleRules(rules); 
    }

    runMonitoring(restrictedAppsMap.size()>0 ? true : false);
    
  }

  @Override
  public void terminate() {
    if (restrictedAppsMap != null) {
      restrictedAppsMap.clear();
    }
    runMonitoring(false);
    
    if (accessCategoryList != null) {
      for (AccessCategory accessCate : accessCategoryList) {
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
      monitorTimer = new Timer();
      if (monitorTask == null) {
        monitorTask = getMonitorTask();
      } 
      monitorTimer.schedule(monitorTask, 0, MONITORTASK_PERIOD);
      
    } else {
      if (monitorTask != null) {
        monitorTask.cancel();
        monitorTask = null;
      }
      if (monitorTimer != null) {
        monitorTimer.purge();
        monitorTimer.cancel();
        monitorTimer = null;
      }
    }
  }
  
  public void setRestrictedAppList(List<ClientAppInfo> appList) {
    boolean append = false;
    _setRestrictedAppList(this.restrictedAppsMap, appList, append);
  }

  public void appendRestrictedAppList(List<ClientAppInfo> appList) {
    boolean append = true;
    _setRestrictedAppList(this.restrictedAppsMap, appList, append);
  }
  
  public void appendRestrictedApp(ClientAppInfo appInfo) {
    ArrayList<ClientAppInfo> appList = new ArrayList<ClientAppInfo>(1);
    boolean append = true;
    _setRestrictedAppList(this.restrictedAppsMap, appList, append);
  }
  
  public void removeRestrictedAppList(List<ClientAppInfo> appList) {
    if (appList==null || appList.size()==0) return;
    
    for (ClientAppInfo appInfo : appList) {
      synchronized(restrictedAppsMap) {
        restrictedAppsMap.remove(appInfo.getAppClassname());
      }
    }
  }
  
  public void killRestrictedApps() {
    List<RunningAppProcessInfo> processes = activityManager
        .getRunningAppProcesses();

    synchronized (restrictedAppsMap) {
      for (RunningAppProcessInfo process : processes) {
        String pname = process.processName;
        if (restrictedAppsMap.containsKey(pname)
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
