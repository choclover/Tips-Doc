package com.studentpal.app;

import java.util.List;

import com.studentpal.engine.ClientEngine;
import com.studentpal.model.exception.STDException;
import com.studentpal.util.logger.Logger;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class MainAppService extends Service {
  private static final String TAG = "MainAppService";
  
  /* 
   * Contants
   */
  public static final int CMD_START_WATCHING_APP = 100;
  public static final int CMD_STOP_WATCHING_APP = 101;
  
  /* 
   * Field members
   */
  private ClientEngine engine = null;
  
  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }
  
  @Override
  public void onCreate() {
    Logger.w(TAG, "onCreate()!");
    super.onCreate();
  }

  // This is the old onStart method that will be called on the pre-2.0
  // platform. On 2.0 or later we override onStartCommand() so this
  // method will not be called.
  @Override
  public void onStart(Intent intent, int startId) {
    onStartCommand(intent, 0, startId);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Logger.w(TAG, "onStartCommand()!\tstartId: "+startId);

    // No intent, tell the system not to restart us.
    if (intent == null) {
      stopSelf();
      return START_NOT_STICKY;
    }

    handleCommand(intent);
    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    return START_STICKY;
  }
  
  @Override
  public void onDestroy() {
    engine = ClientEngine.getInstance();
    if (engine != null) {
      engine.terminate();
    }
    
    super.onDestroy();  
  }
  
  private void handleCommand(Intent intent) {
    int cmd = intent.getIntExtra("command", -1);
    switch (cmd) {
    case CMD_START_WATCHING_APP:
      engine = ClientEngine.getInstance();
      try {
        engine.initialize(this);
        engine.launch();
      } catch (STDException e) {
        e.printStackTrace();
      }
      break;
      
    case CMD_STOP_WATCHING_APP:
      break;
      
    default:
      break;
    }
  }

  /*
   * 判断服务是否运行.
   * @param context
   * @param className 判断的服务名字
   */
  public static boolean isServiceRunning(Context mContext, String className) {
    boolean isRunning = false;
    ActivityManager activityManager = (ActivityManager) mContext
        .getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningServiceInfo> serviceList = activityManager
        .getRunningServices(30);

    for (int i = 0; i < serviceList.size(); i++) {
      if (serviceList.get(i).service.getClassName().equals(className) == true) {
        isRunning = true;
        break;
      }
    }
    return isRunning;
  }
  
}
