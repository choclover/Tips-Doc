package com.studentpal.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.studentpal.app.AccessController;
import com.studentpal.app.MainAppService;
import com.studentpal.app.MessageHandler;
import com.studentpal.app.ResourceManager;
import com.studentpal.app.io.IoHandler;
import com.studentpal.app.receiver.SystemStateReceiver;
import com.studentpal.engine.request.LoginRequest;
import com.studentpal.engine.request.Request;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.model.exception.STDException;
import com.studentpal.ui.AccessDeniedNotification;
import com.studentpal.ui.AccessRequestForm;
import com.studentpal.util.Utils;
import com.studentpal.util.logger.Logger;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

public class ClientEngine implements AppHandler {
  
  private static final String TAG = "ClientEngine";
  
  /* 
   * Field Members
   */
  private static ClientEngine instance = null;
  
  private Context launcher;
  private SystemStateReceiver sysStateReceiver = null;
  private ActivityManager activityManager = null;
  private TelephonyManager teleManager = null;
  //Handlers
  private MessageHandler msgHandler = null;
  private IoHandler ioHandler = null;
  private AccessController accController = null;
  private List<AppHandler> appHandlerAry = null;
  
  private ClientEngine() {
  }
  
  public static ClientEngine getInstance() {
    if (instance == null) {
      instance = new ClientEngine();
    }
    return instance;
  }

  public void initialize(Context context) throws STDException {
    if (context == null) {
      throw new STDException("Context launcher should NOT be NULL");
    } else {
      this.launcher = context;
    }
    
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
    
    //Create AccessController instance
    this.accController = AccessController.getInstance();
    
    if (appHandlerAry == null) {
      appHandlerAry = new ArrayList<AppHandler>();
      appHandlerAry.add(msgHandler);
      appHandlerAry.add(ioHandler);
      appHandlerAry.add(accController);
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////
  @Override
  public void terminate() {
    if (appHandlerAry != null) {
      for (AppHandler handler : appHandlerAry) {
        if (handler != null) {
          handler.terminate();
        }
      }
    }
    
    if (launcher != null) {
      if (sysStateReceiver != null) {
        launcher.unregisterReceiver(sysStateReceiver);
      }
    }
    
  }
  
  @Override
  public void launch() {
    for (AppHandler handler : appHandlerAry) {
      if (handler != null) {
        handler.launch();
      }
    }
  }
  
  public String getPhoneNum() {
    String result = "";
    if (this.teleManager != null) {
      result = this.teleManager.getLine1Number();
    }
    return result;
  }

  public Context getContext() {
    return launcher;
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
  
  public AccessController getAccessController() {
    return accController;
  }
  
  public PackageManager getPackageManager() {
    return launcher.getPackageManager();
  }
  
  public int getApiVersion() {
    return android.os.Build.VERSION.SDK_INT;
  }
  
  public void launchNewActivity(Class<?> activity) {
    if (activity != null) {
      Intent i = new Intent(this.launcher, activity);
      i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      this.launcher.startActivity(i);
    }
  }
  
  public void returnToHomeScreen() {
    Logger.v(TAG, "enter returnToHomeScreen!");
    Intent startMain = new Intent(Intent.ACTION_MAIN);
    startMain.addCategory(Intent.CATEGORY_HOME);
    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    this.launcher.startActivity(startMain);  
  }
  
  public void showAccessDeniedNotification() {
    AlertDialog.Builder builder = new AlertDialog.Builder(launcher);
    builder.setMessage(ResourceManager.RES_STR_OPERATION_DENIED);
    builder.setCancelable(false);
    builder.setPositiveButton(ResourceManager.RES_STR_SENDREQUEST, 
      new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          Intent i = new Intent(launcher, AccessRequestForm.class);
          i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          launcher.startActivity(i);
        }
    });
    builder.setNegativeButton(ResourceManager.RES_STR_CANCEL, 
      new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          dialog.cancel();
        }
    });
    
    AlertDialog alert = builder.create();
    alert.show();
  }
  
  public List<ClientAppInfo> getAppList() {
    List<ApplicationInfo> applications = getPackageManager().getInstalledApplications(0);
    List<ClientAppInfo> result = new ArrayList<ClientAppInfo>(applications.size());
    
    Iterator<ApplicationInfo> iter = applications.iterator();
    while (iter.hasNext()) {
      ClientAppInfo clientApp = new ClientAppInfo((ApplicationInfo) iter.next());
      result.add(clientApp);
      Logger.d(TAG, "Adding AppInfo with name: "+clientApp.getAppName()
                +", \tClassName: "+clientApp.getAppClassname());
    }
    
    return result;
  }

  public void loginServer() throws STDException {
    Logger.i(TAG, "enter loginServer");
    
    String phoneNum = getPhoneNum();
    if (! Utils.isValidPhoneNumber(phoneNum)) {
      throw new STDException("Got invalid phone number of " + phoneNum
          + ", unable to login!");
    }
    
    Request request = new LoginRequest(phoneNum);
    msgHandler.sendRequest(request);
  }
}