package com.studentpal.app;

import static com.studentpal.app.ResourceManager.APPLICATION_PKG_NAME;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.studentpal.engine.ClientEngine;
import com.studentpal.engine.Event;
import com.studentpal.model.exception.STDException;
import com.studentpal.util.logger.Logger;

public class MainAppService extends Service {
  private static final String TAG = "@@ MainAppService";
  public static final String NAME = APPLICATION_PKG_NAME + ".app.MainAppService";
  
  /* 
   * Constants
   */
  public static final int CMD_START_WATCHING_APP = 100;
  public static final int CMD_STOP_WATCHING_APP = 101;
  
  public static final boolean forTest = true;

  /* 
   * Field members
   */
  private ClientEngine engine = null;
  
  @Override
  public void onCreate() {
    Logger.d(TAG, "onCreate()!");
    super.onCreate();
    
    engine = ClientEngine.getInstance();
  }

  // This is the old onStart method that will be called on the pre-2.0
  // platform. On 2.0 or later we override onStartCommand() so this
  // method will not be called.
  @Override
  public void onStart(Intent intent, int startId) {
    Logger.d(TAG, "onStart()!");
    onStartCommand(intent, 0, startId);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Logger.d(TAG, "onStartCommand()!\tstartId: "+startId);

    // No intent, tell the system not to restart us.
    if (intent == null) {
      Logger.d(TAG, "Intent should NOT be NULL in onStartCommand(), exiting...");
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
    Logger.d(TAG, "onDestroy()!");
    if (engine != null) {
      engine.terminate();
    }
    
    super.onDestroy();  
  }
  
  @Override
  public IBinder onBind(Intent arg0) {
    Logger.d(TAG, "onBind()!");
    return null;
  }
  
//  @Override  
//  public boolean onUnbind(Intent intent) {
//    Logger.d(TAG, "onUnbind...");
//    return super.onUnbind(intent);
//  }
  
  //////////////////////////////////////////////////////////////////////////////
  private void handleCommand(Intent intent) {
    int cmd = intent.getIntExtra(Event.TAGNAME_BUNDLE_PARAM, -1);
    switch (cmd) {
    case CMD_START_WATCHING_APP:
      try {
        engine.initialize(this);
        engine.launch();
        
      } catch (STDException e) {
        Logger.w(TAG, e.toString());
      }
      break;
      
    case CMD_STOP_WATCHING_APP:
      break;
      
    default:
      break;
    }
  }
  
}
