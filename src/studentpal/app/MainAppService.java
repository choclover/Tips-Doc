package studentpal.app;

import studentpal.engine.ClientEngine;
import studentpal.model.exception.STDException;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MainAppService extends Service {
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
    super.onCreate();
  }

  // This is the old onStart method that will be called on the pre-2.0
  // platform. On 2.0 or later we override onStartCommand() so this
  // method will not be called.
  @Override
  public void onStart(Intent intent, int startId) {
    handleCommand(intent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    handleCommand(intent);
    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    return START_STICKY;
  }
  
  private void handleCommand(Intent intent) {
    int cmd = intent.getIntExtra("command", -1);
    switch (cmd) {
    case CMD_START_WATCHING_APP:
      engine = ClientEngine.getInstance();
      try {
        engine.launch(this);
      } catch (STDException e) {
        e.printStackTrace();
      }
      break;
      
    case CMD_STOP_WATCHING_APP:
      //TODO: stop program watching thread 
      break;
    }
  }
  
  
}
