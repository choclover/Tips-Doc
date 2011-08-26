package com.studentpal.app;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.studentpal.app.io.IoHandler;
import com.studentpal.engine.AppHandler;
import com.studentpal.engine.ClientEngine;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.util.logger.Logger;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;


public class AccessController implements AppHandler {
  /*
   * Field constants
   */
  private static final String TAG = "AccessController";
  private static final int TIMERTASK_PERIOD = 2000;  //mill-seconds

  /*
   * Field members
   */
  private static AccessController instance = null;
  private ClientEngine  engine = null;
  private ActivityManager activityManager = null;
  
  
  private boolean   isStopped    = false;
  private Timer     monitorTimer = null;
  private TimerTask monitorTask = null;
  private HashMap<String, String> restrictedAppsMap; 
  
  /*
   * Methods
   */
  private AccessController() {
    initialize();
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
    restrictedAppsMap = new HashMap<String, String>();
    this.engine = ClientEngine.getInstance();
    this.activityManager = engine.getActivityManager();
    
    //TODO read restricted app list from config
    List<ClientAppInfo> appList = null;
    setRestrictedAppList(appList, false);
  }

  @Override
  public void terminate() {
    // TODO Auto-generated method stub
    
  }
  
  public void start() {
    isStopped = false;
  }
  public void stop() {
    isStopped = true;
  }
  
  public void runMonitoring(boolean bMonitor) {
    if (isStopped==false && bMonitor==true) {
      monitorTimer = new Timer();
      if (monitorTask == null) {
        monitorTask = getMonitorTask();
      } 
      monitorTimer.schedule(monitorTask, 0, TIMERTASK_PERIOD);
      
    } else {
      if (monitorTask != null) monitorTask.cancel();
      if (monitorTimer != null) monitorTimer.cancel();
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
          restrictedAppsMap.put(appInfo.getAppName(), appInfo.getAppClassname());
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
            && process.pkgList.equals(restrictedAppsMap.get(pname))) {
          Logger.i(TAG, "Ready to kill application: " + pname);
          
          killProcess(process);
          // if (true) { // for version before 2.2
          // // android.os.Process.killProcess(process.pid);
          // String[] pkgs = process.pkgList;
          // for (String pkg : pkgs) {
          // am.restartPackage(pkg);
          // }
          // }
        }
      }
    }
  }
  
  private boolean killProcess(RunningAppProcessInfo p) {
    return false;
  }

}
