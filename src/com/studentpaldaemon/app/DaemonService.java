package com.studentpaldaemon.app;

import static com.studentpal.engine.Event.SIGNAL_TYPE_DAEMON_WD_REQ;
import static com.studentpal.engine.Event.SIGNAL_TYPE_DAEMON_WD_RESP;
import static com.studentpal.engine.Event.SIGNAL_TYPE_DAEMON_WD_TIMEOUT;
import static com.studentpal.engine.Event.SIGNAL_TYPE_EXIT_DAEMONTASK;
import static com.studentpal.engine.Event.SIGNAL_TYPE_START_DAEMONTASK;
import static com.studentpal.engine.Event.SIGNAL_TYPE_STOP_DAEMONTASK;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.studentpal.app.MainAppService;
import com.studentpal.app.ResourceManager;
import com.studentpal.app.handler.DaemonHandler;
import com.studentpal.engine.Event;

import com.studentpaldaemon.app.receiver.MyDeviceAdminReceiver;
import com.studentpaldaemon.util.logger.Logger;

public class DaemonService extends Service {
  private static final String TAG = "@@ DaemonService";
  private static final boolean forTest = true;
  
  /* 
   * Field members
   */
  private ActivityManager      activityManager = null;
  private Timer                _watchdogTimer  = null;
  private TimerTask            _watchdogTask   = null;

  /**
   * Target we publish for clients to send messages to IncomingHandler.
   */
  final IncomingHandler msgHandler = new IncomingHandler();
  final Messenger mMsgerToMyself = new Messenger(msgHandler );
  /** Messenger for communicating with MainApp service. */
  private Messenger mMsgerToMainApp = null;
  
  //For test
  //private boolean stopCounterThd = false;
  
  /** 
   * Called when the activity is first created. 
   */
  @Override
  public void onCreate() {
    Logger.d(TAG, "onCreate!");
    
    //Enable the Device Administration 
    try {
      MyDeviceAdminReceiver mAdminReceiver = new MyDeviceAdminReceiver(new Activity());
      mAdminReceiver.enableAdmin();
    } catch (Exception e) {
      Logger.w(TAG, e.toString());
    }
    
    super.onCreate(); 
  } 
  
  //This is the old onStart method that will be called on the pre-2.0
  // platform. On 2.0 or later we override onStartCommand() so this
  // method will not be called.
  @Override
  public void onStart(Intent intent, int startId) {
    Logger.d(TAG, "onStart!");
    onStartCommand(intent, 0, startId);
  }
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Logger.d(TAG, "onStartCommand!");
    
    // No intent, tell the system not to restart us.
    if (intent == null) {
      stopSelf();
      return START_NOT_STICKY;
    }
    
    if (forTest) {
      startConterThread();
    }
    
    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent arg0) {
    Logger.d(TAG, "onBind!");
    return mMsgerToMyself.getBinder();
  }
  
  public void onRebind (Intent arg0) {
    Logger.d(TAG, "onRebind!");
  }
  
  public boolean onUnind(Intent arg0) {
    Logger.d(TAG, "onUnind!");
    return true;
  }
  
  @Override
  public void onDestroy() {
    Logger.d(TAG, "onDestroy(), stopping myself!");
    
    stopConterThread();
    
    runWatchdogTask(false);
    mMsgerToMainApp = null;
    stopSelf();
    
    exitApp();
  }

  // ///////////////////////////////////////////////////////////////////////////
  static class CounterThread {
    private static boolean stop = false;
    private static Thread  inner_th = null;

    public static void stop() {
      Logger.d(TAG, "CounterThread.stop()");
      stop = true;
      if (inner_th != null) {
        inner_th.interrupt();
        inner_th = null;
      }
    }

    public static void start() {
      Logger.d(TAG, "CounterThread.start()");
      if (inner_th != null) return;
      
      stop = false;
      inner_th = new Thread(new Runnable() {
        @Override
        public void run() {
          int counter = 1;
          while (false == stop) {
            if (counter > 10000) counter=1;
            Logger.v(TAG, "Daemon is running @ " + counter++);

            try { Thread.sleep(3000); }
            catch (InterruptedException e) { }
          }
          Logger.v(TAG, "Counter thread " + this + " is stopped!");
        }
      });
      inner_th.start();
    }
  }
  
  private void startConterThread() {
    if (forTest) CounterThread.start();
  }
  private void stopConterThread() {
    if (forTest) CounterThread.stop();
  }
  
  private TimerTask getWatchdogTask() {
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        //Logger.v(TAG, "Daemon watchdog task starts to run!");
        sendWatchdogMsg();
      }
    };
    return task;
  }
  
  private void sendWatchdogMsg() {
    if (mMsgerToMainApp == null) {
      Logger.w(TAG, "mMsgerToMainApp should NOT be NULL!");
      return;
    }
    
    try {
      Message wdMsg = Message.obtain(null, SIGNAL_TYPE_DAEMON_WD_REQ);
      this.mMsgerToMainApp.send(wdMsg);
      this.msgHandler.sendEmptyMessageDelayed(SIGNAL_TYPE_DAEMON_WD_TIMEOUT, 
          DaemonHandler.DAEMON_WATCHDOG_TIMEOUT);
    } catch (RemoteException e) {
      Logger.w(TAG, "sendWatchdogReq() got exception with "+e.toString());      
      // The client is dead.
      runWatchdogTask(false);
      reLaunchClientApp();
    }
  }
  
  public void runWatchdogTask(boolean runWD) {
    Logger.d(TAG, "Ready to run watchdog is: "+runWD);
    
    if (runWD==true) {
      //we cannot reuse the old Timer if it is ever cancelled, so have to recreate one
        if (_watchdogTimer != null) {
        _watchdogTimer.purge();
        _watchdogTimer.cancel();
      }
      _watchdogTimer = new Timer();
      
      if (_watchdogTask == null) {
        _watchdogTask = getWatchdogTask();
      } else {
        _watchdogTask.cancel();
      }
      _watchdogTimer.schedule(_watchdogTask, 0, DaemonHandler.DAEMON_WATCHDOG_INTERVAL);
      
    } else {
      msgHandler.removeMessages(SIGNAL_TYPE_DAEMON_WD_TIMEOUT);
      
      if (_watchdogTask != null) {
        _watchdogTask.cancel();
        _watchdogTask = null;
      }
      if (_watchdogTimer != null) {
        _watchdogTimer.purge();
        _watchdogTimer.cancel();
        _watchdogTimer = null;
      }
    }
  }
  
  private void handleClientTimeout() {
    Logger.d(TAG, "handleClientTimeout()");
    
    msgHandler.removeMessages(SIGNAL_TYPE_DAEMON_WD_TIMEOUT);
    reLaunchClientApp();
  }
  
  private boolean reLaunchClientApp() {
    boolean result = true;
    
    String procName = ResourceManager.APPLICATION_PKG_NAME;
    if (findRunningAppProcess(procName) == null) {
      Logger.w(TAG, procName + " is NOT running, relaunching it!");

      Intent intent = new Intent();
//      Bundle bundle = new Bundle();
//      bundle.putBoolean(CFG_SHOW_LAUNCHER_UI, false);
//      intent.putExtras(bundle);
      ComponentName comp = new ComponentName(procName,
          MainAppService.NAME);
      intent.setComponent(comp);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(Event.TAGNAME_BUNDLE_PARAM, MainAppService.CMD_START_WATCHING_APP);
      
      startService(intent);  //hemerr
      
    } else {
      Logger.d(TAG, procName + " is still running!!");
    }
    
    return result;
  }
  
  /**
   * Utility Functions -- will moved to common class shared with MainAppService
   */
  private RunningAppProcessInfo findRunningAppProcess(String appProcName) {
    RunningAppProcessInfo result = null;

    if (activityManager == null) {
      activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
    }
    
    List<RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
    if (processes != null) {
      for (RunningAppProcessInfo process : processes) {
        String pname = process.processName;
        // Logger.d(TAG, pname);
        if (appProcName.equals(pname)) {
          result = process;
          break;
        }
      }
    }
    return result;
  }
  
  private void exitApp() {
    int pid = android.os.Process.myPid();
    android.os.Process.killProcess(pid);
    //System.exit(1);
  }
  
  // ///////////////////////////////////////////////////////////////////////////
  /**
   * Handler of incoming messages from clients.
   */
  class IncomingHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      int sigType = msg.what;
      //Logger.d(TAG, "Signal type is: "+sigType);
      switch(sigType) {
        case SIGNAL_TYPE_START_DAEMONTASK:
          mMsgerToMainApp = msg.replyTo;
          runWatchdogTask(true);
          break;
          
        case SIGNAL_TYPE_STOP_DAEMONTASK:
          runWatchdogTask(false);
          stopConterThread();
          break;
        
        case SIGNAL_TYPE_EXIT_DAEMONTASK:
          onDestroy();
          break;
       
        case SIGNAL_TYPE_DAEMON_WD_RESP:
          /* received watch dog response from MainAppService, so remove Timeout
           * signal and send out next watch dog request later.
           */
          this.removeMessages(SIGNAL_TYPE_DAEMON_WD_TIMEOUT);
          break; 
          
        case SIGNAL_TYPE_DAEMON_WD_TIMEOUT:
          runWatchdogTask(false);
          handleClientTimeout();
          break;
          
        default:
          super.handleMessage(msg);
        
      }
    }
  }

}