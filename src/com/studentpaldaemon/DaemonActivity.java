package com.studentpaldaemon;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class DaemonActivity extends Activity {
  private static final String TAG = "@@ DaemonActivity";
  
  ActivityManager activityManager = null;
  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
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
}