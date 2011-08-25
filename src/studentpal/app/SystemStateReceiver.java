package studentpal.app;

import studentpal.util.logger.Logger;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SystemStateReceiver extends BroadcastReceiver {
  private static final String TAG = "SystemStateReceiver";

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    Log.i(TAG, "Action onReceive() is:" + action);

    if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
      Logger.i(TAG, "==== SYSTEM BOOT COMPLETED ====");

      Intent launcherIntent = new Intent(context,
          studentpal.ui.LaunchScreen.class);
      launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(launcherIntent);

    } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
      Log.i(TAG, "Screen is turned ON");
      
      // Intent i = new Intent(context,
      // com.hemi.helloworld.InstalledProgramsListService.class);
      // i
      // .putExtra("command",
      // InstalledProgramsListService.CMD_START_WATCHING_APP);
      // context.startService(i);

    } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
      Log.i(TAG, "Screen is turned OFF");
      
      // Intent i = new Intent(context,
      // com.hemi.helloworld.InstalledProgramsListService.class);
      // i.putExtra("command",
      // InstalledProgramsListService.CMD_STOP_WATCHING_APP);
      // context.startService(i);
    }
  }

}