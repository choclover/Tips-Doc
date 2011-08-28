package com.studentpal.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.studentpal.engine.AppHandler;
import com.studentpal.engine.ClientEngine;
import com.studentpal.model.ClientAppInfo;
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
  
  private boolean   isStopped    = false;
  private Timer     monitorTimer = null;
  private TimerTask monitorTask  = null;
  private HashMap<String, String> restrictedAppsMap; 
  
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
    //TODO
  }
  
  @Override
  public void launch() {
    isStopped = false;
    this.engine = ClientEngine.getInstance();
    this.activityManager = engine.getActivityManager();

    if (restrictedAppsMap != null) {
      restrictedAppsMap.clear();
    }
    restrictedAppsMap = new HashMap<String, String>();
    
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
    setRestrictedAppList(appList, false);
  }

  @Override
  public void terminate() {
    stop();
    runMonitoring(false);
  }
  
  public void start() {
    isStopped = false;
  }
  public void stop() {
    isStopped = true;
  }
  
  public void runMonitoring(boolean runMonitor) {
    Logger.i(TAG, "Ready to run monitoring is: "+runMonitor);
    
    if (isStopped==false && runMonitor==true) {
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
        monitorTimer.cancel();
        monitorTimer = null;
      }
    }
  }
  
  public void setRestrictedAppList(List<ClientAppInfo> appList, boolean append) {
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
    
    runMonitoring(restrictedAppsMap.size()>0 ? true : false);
  }
  
  public void appendRestrictedAppList(List<ClientAppInfo> appList) {
    setRestrictedAppList(appList, true);
  }
  
  //////////////////////////////////////////////////////////////////////////////
  private TimerTask getMonitorTask() {
    TimerTask task = new TimerTask() {
      public void run() {
        Logger.d(TAG, "Monitor Task starts to run!");
        killRestrictedApps();
      }
    };
    return task;
  }
  
  private void killRestrictedApps() {
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
