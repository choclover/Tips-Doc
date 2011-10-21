package com.studentpaldaemon.app;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class DaemonService extends Service {
  private static final String TAG = "@@ DaemonService";
  
  ActivityManager activityManager = null;
  
  /**
   * Target we publish for clients to send messages to IncomingHandler.
   */
  final Messenger mMessenger = new Messenger(new IncomingHandler());

  
    /** Called when the activity is first created. */
    @Override
    public void onCreate() {
        super.onCreate();
//        setContentView(R.layout.main);
//        
        activityManager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
            int i = 1;
            while (true) {
              android.util.Log.v(TAG, "I am running @ " + i++);
              
              String procName = "com.studentpal";
              if (findRunningAppProcess(procName) == null) {
                android.util.Log.d(TAG, procName + " is NOT running!!");
                
                Intent intent = new Intent();
                ComponentName comp = new ComponentName("com.studentpal",
                    "com.studentpal.ui.LaunchScreen");
                intent.setComponent(comp);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putBoolean("show_launcher_ui", false);
                intent.putExtras(bundle);
                
                startActivity(intent);
              }
              
              try {
                Thread.sleep(500);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
          }
        });
        t.start();
    }
    
    private RunningAppProcessInfo findRunningAppProcess(String classname) {
      RunningAppProcessInfo result = null;
      
      List<RunningAppProcessInfo> processes = activityManager
          .getRunningAppProcesses();
      for (RunningAppProcessInfo process : processes) {
        String pname = process.processName;
        //android.util.Log.d(TAG, pname);
        if (classname.equals(pname)) {
          result = process;
          break;
        }
      }
      return result;
    }

    @Override
    public IBinder onBind(Intent arg0) {
      return mMessenger.getBinder();

    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        }
    }

}