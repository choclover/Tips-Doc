package com.studentpal.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

import com.studentpal.app.AccessController;
import com.studentpal.app.MessageHandler;
import com.studentpal.app.ResourceManager;
import com.studentpal.app.db.DBaseManager;
import com.studentpal.app.io.IoHandler;
import com.studentpal.app.receiver.SystemStateReceiver;
import com.studentpal.engine.request.LoginRequest;
import com.studentpal.engine.request.Request;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.model.exception.STDException;
import com.studentpal.ui.AccessRequestForm;
import com.studentpal.util.Utils;
import com.studentpal.util.logger.Logger;

public class ClientEngine implements AppHandler {
  
  private static final String TAG = "ClientEngine";
  
  /*
   * Field Members
   */
  private static ClientEngine instance = null;
  
  private Context             _launcher;
  private PackageManager      _packageManager   = null;
  private SystemStateReceiver _sysStateReceiver = null;
  private ActivityManager     _activityManager  = null;
  private TelephonyManager    _teleManager      = null;
  
  //Handlers
  private List<AppHandler>    appHandlerAry     = null;
  private MessageHandler      msgHandler        = null;
  private IoHandler           ioHandler         = null;
  private AccessController    accController     = null;
  private DBaseManager        dbaseManager      = null;
  
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
      this._launcher = context;
    }
    
    this._activityManager = (ActivityManager)this._launcher.getSystemService(Context.ACTIVITY_SERVICE);
    
    //Register System State Broadcast receiver
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
    intentFilter.addAction(Intent.ACTION_SCREEN_ON);
    this._sysStateReceiver = new SystemStateReceiver();
    this._launcher.registerReceiver(_sysStateReceiver, intentFilter);

    //Create Telephony Manager
    this._teleManager = (TelephonyManager)this._launcher.getSystemService(Context.TELEPHONY_SERVICE);
    
    //Create MessageHandler instance
    this.msgHandler = MessageHandler.getInstance();
    
    //Create IoHandler instance
    this.ioHandler =  IoHandler.getInstance();
    
    //Create AccessController instance
    this.accController = AccessController.getInstance();
    
    //Create DBaseManager instance
    this.dbaseManager = DBaseManager.getInstance();
    
    if (appHandlerAry == null) {
      appHandlerAry = new ArrayList<AppHandler>();
      appHandlerAry.add(msgHandler);
      appHandlerAry.add(ioHandler);
      appHandlerAry.add(accController);
      appHandlerAry.add(dbaseManager);
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
    
    if (_launcher != null) {
      if (_sysStateReceiver != null) {
        _launcher.unregisterReceiver(_sysStateReceiver);
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
    if (this._teleManager != null) {
      result = this._teleManager.getLine1Number();
    }
    return result;
  }

  public Context getContext() {
    return _launcher;
  }
  
  public ActivityManager getActivityManager() {
    return _activityManager;
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
  
  public DBaseManager getDBaseManager() {
    return dbaseManager;
  }
  
  public PackageManager getPackageManager() {
    if (_packageManager == null) {
      _packageManager = _launcher.getPackageManager();
    }
    return _packageManager;
  }
  
  public int getApiVersion() {
    return android.os.Build.VERSION.SDK_INT;
  }
  
  public void launchNewActivity(Class<?> activity) {
    if (activity != null) {
      Intent i = new Intent(this._launcher, activity);
      i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      this._launcher.startActivity(i);
    }
  }
  
  public void returnToHomeScreen() {
    Logger.v(TAG, "enter returnToHomeScreen!");
    Intent startMain = new Intent(Intent.ACTION_MAIN);
    startMain.addCategory(Intent.CATEGORY_HOME);
    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    this._launcher.startActivity(startMain);
  }
  
  public void showAccessDeniedNotification() {
    AlertDialog.Builder builder = new AlertDialog.Builder(_launcher);
    builder.setMessage(ResourceManager.RES_STR_OPERATION_DENIED);
    builder.setCancelable(false);
    builder.setPositiveButton(ResourceManager.RES_STR_SENDREQUEST,
      new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          Intent i = new Intent(_launcher, AccessRequestForm.class);
          i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          _launcher.startActivity(i);
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
    //List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
    
    List<ClientAppInfo> result = new ArrayList<ClientAppInfo>(applications.size());
    
    Iterator<ApplicationInfo> iter = applications.iterator();
    while (iter.hasNext()) {
      ClientAppInfo clientApp = new ClientAppInfo(iter.next());
      result.add(clientApp);
      Logger.d(TAG, "Adding AppInfo with name: "+clientApp.getAppName()
                +", \nPackageName: "+clientApp.getAppPkgname()
                +", \nClassName: "+clientApp.getAppClassname()
                );
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