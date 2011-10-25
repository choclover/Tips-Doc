package com.studentpal.app.receiver;

import com.studentpal.engine.ClientEngine;
import com.studentpal.engine.Event;
import com.studentpal.util.logger.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SystemStateReceiver extends BroadcastReceiver {
  private static final String TAG = "@@ SystemStateReceiver";
  private static final boolean forDeploy = false;

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    Logger.i(TAG, "Action onReceive() is:" + action);

    if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
      Logger.i(TAG, "==== SYSTEM BOOT COMPLETED ====");

      if (forDeploy) {
        Intent launcherIntent = new Intent(context,
            com.studentpal.ui.LaunchScreen.class);
        launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launcherIntent);
      }

    } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
      Logger.i(TAG, "Screen is turned ON");
      try {
        ClientEngine.getInstance().getAccessController().runMonitoring(true);
        ClientEngine.getInstance().getDaemonHandler()
            .sendMsgToDaemon(Event.SIGNAL_TYPE_START_DAEMONTASK);
      } catch (Exception e) {
        Logger.w(TAG, e.toString());
      }

    } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
      Logger.i(TAG, "Screen is turned OFF");
      
      try {
        ClientEngine.getInstance().getAccessController().runMonitoring(false);
        ClientEngine.getInstance().getDaemonHandler()
            .sendMsgToDaemon(Event.SIGNAL_TYPE_STOP_DAEMONTASK);
      } catch (Exception e) {
        Logger.w(TAG, e.toString());
      }
    }
  }

}