package studentpal.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import studentpal.app.MessageHandler;
import studentpal.app.SystemStateReceiver;
import studentpal.app.io.IoHandler;
import studentpal.model.ClientAppInfo;
import studentpal.model.exception.STDException;
import studentpal.util.logger.Logger;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ClientEngine implements AppHandler {
  
  private static final String TAG = "ClientEngine";
  
  /* 
   * Field Members
   */
  private static ClientEngine instance = null;
  
  private Context launcher;
  private ActivityManager activityManager = null;
  private TelephonyManager teleManager = null;
  private SystemStateReceiver sysStateReceiver = null;
  private MessageHandler msgHandler = null;
  private IoHandler ioHandler = null;

  private ClientEngine() {
    initialize();
  }
  
  public static ClientEngine getInstance() {
    if (instance == null) {
      instance = new ClientEngine();
    }
    return instance;
  }

  private void initialize() {
    this.activityManager = (ActivityManager)this.launcher.getSystemService(Context.ACTIVITY_SERVICE);
    
    //Register System State Broadcast receiver
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
    intentFilter.addAction(Intent.ACTION_SCREEN_ON);
    this.sysStateReceiver = new SystemStateReceiver();
    this.launcher.registerReceiver(sysStateReceiver, intentFilter);

    //Create Telephony Manager
    this.teleManager = (TelephonyManager)this.launcher.getSystemService(Context.TELEPHONY_SERVICE);
    
    //Create MessageHandler instance
    this.msgHandler = MessageHandler.getInstance();
    
    //Create IoHandler instance
    this.ioHandler =  IoHandler.getInstance();
  }
  
  //////////////////////////////////////////////////////////////////////////////
  @Override
  public void launch() {
  
  }
  
  public void launch(Context context) throws STDException {
    if (this.launcher != null) {
      return;  //initialized
    }
    
    if (context == null) {
      throw new STDException("Context launcher should NOT be NULL");
    } else {
      this.launcher = context;
    }
    
    this.ioHandler.launch();
    this.msgHandler.launch();
  }
  
  public String getPhoneNum() {
    return this.teleManager.getLine1Number();
  }

  public ActivityManager getActivityManager() {
    return activityManager;
  }

  public MessageHandler getMsgHandler() {
    return msgHandler;
  }

  public IoHandler getIoHandler() {
    return ioHandler;
  }
  
  public PackageManager getPackageManager() {
    return launcher.getPackageManager();
  }
  
  public List<ClientAppInfo> getAppList() {
    List<ApplicationInfo> applications = getPackageManager().getInstalledApplications(0);
    List<ClientAppInfo> result = new ArrayList<ClientAppInfo>(applications.size());
    
    Iterator<ApplicationInfo> iter = applications.iterator();
    while (iter.hasNext()) {
      ClientAppInfo clientApp = new ClientAppInfo((ApplicationInfo) iter.next());
      result.add(clientApp);
      Logger.d(TAG, "Added AppInfo with name: "+clientApp.getAppName()
                +", \tClassName: "+clientApp.getAppClassname());
    }
    
    return result;
  }

}