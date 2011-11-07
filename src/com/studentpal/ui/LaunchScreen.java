package com.studentpal.ui;

import static com.studentpal.engine.Event.ACTION_DAEMONSVC_INFO_UPDATED;
import static com.studentpal.engine.Event.ACTION_DAEMON_LAUNCHER_SCR;
import static com.studentpal.engine.Event.ACTION_MAINSVC_INFO_UPDATED;
import static com.studentpal.engine.Event.CFG_SHOW_LAUNCHER_UI;
import static com.studentpal.engine.Event.SIGNAL_TYPE_DEVICE_ADMIN_ENABLED;
import static com.studentpal.engine.Event.TAGNAME_BUNDLE_PARAM;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.studentpal.R;
import com.studentpal.app.MainAppService;
import com.studentpal.app.ResourceManager;
import com.studentpal.app.handler.IoHandler;
import com.studentpal.app.receiver.MyDeviceAdminReceiver;
import com.studentpal.engine.ClientEngine;
import com.studentpal.model.exception.STDException;
import com.studentpal.util.ActivityUtil;
import com.studentpal.util.Utils;
import com.studentpal.util.logger.Logger;

public class LaunchScreen extends Activity {
  private static final String TAG = "LaunchScreen";
  
  /* 
   * Constants
   */
  private static boolean showUI = true;
  
  /*
   * Member fields
   */
  private Button btnStart, btnStop;
  private TextView tvMainSvcStatus, tvMainSvcInfo;
  
  private Button btnStartDae, btnStopDae, btnExitDae;
  private TextView tvDaeSvcStatus, tvDaeSvcInfo;

  private Button btnSendAction, btnInstDaemon;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle cfgParams = getIntent().getExtras();
    if (cfgParams != null) {
      showUI = cfgParams.getBoolean(CFG_SHOW_LAUNCHER_UI, showUI) ;
    }
    Logger.d(TAG, "showUI is set to: "+showUI);
    
    enableDeviceAdmin();
    
    if (showUI) {
      setContentView(R.layout.launcher_screen);
      initMainSvcView();
      initDaemonSvcView();
      
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(ACTION_MAINSVC_INFO_UPDATED);
      intentFilter.addAction(ACTION_DAEMONSVC_INFO_UPDATED);
      registerReceiver(new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          if (intent == null) {
            Logger.w(TAG, "Intent should NOT be NULL");
            return;
          }
          
          String action = intent.getAction();
          String info = intent.getStringExtra("info");
          //Logger.d(TAG, "Received action in " + action);

          if (action.equals(ACTION_MAINSVC_INFO_UPDATED)) {
            tvMainSvcInfo.setText(info);
          } else if (action.equals(ACTION_DAEMONSVC_INFO_UPDATED)) {
            tvDaeSvcInfo.setText(info);
          } 
     
        }
      }, intentFilter); 
      
     
    } else {
      _startWatchingService();
      _startDaemonService();
      this.finish();
    }
    
  }

  @Override
  public void onBackPressed() {
    ActivityUtil.showQuitAppDialog(this);
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case SIGNAL_TYPE_DEVICE_ADMIN_ENABLED:
      if (resultCode == Activity.RESULT_OK) {
        Logger.i(TAG, "Enable Admin OK!");
      } else {
        Logger.i(TAG, "Enable Admin FAILED!");
      }
      return;
    }

    super.onActivityResult(requestCode, resultCode, data);
  }
  
  //////////////////////////////////////////////////////////////////////////////
  private void enableDeviceAdmin() {
    //Enable the Device Administration 
    try {
      MyDeviceAdminReceiver mAdminReceiver =  new MyDeviceAdminReceiver(this);
      if (false) mAdminReceiver.enableAdmin();  //FIXME
      
      if (true == ActivityUtil.checkAppIsInstalled(this, 
          ResourceManager.DAEMON_SVC_PKG_NAME)) {
        Intent daemonIntent = new Intent();
        daemonIntent.setAction(ACTION_DAEMON_LAUNCHER_SCR);  
        this.startActivity(daemonIntent);
      }
    } catch (STDException e) {
      Logger.w(TAG, e.toString());
    }
  }
  
  private void _startWatchingService() {
    if (ActivityUtil.checkServiceIsRunning(this, MainAppService.class.getName())) {
      Logger.d(TAG, "Watching Service is already running!");
      return;
    }
    
    Intent i = new Intent(this, com.studentpal.app.MainAppService.class);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    i.putExtra(TAGNAME_BUNDLE_PARAM, com.studentpal.app.MainAppService.CMD_START_WATCHING_APP);
    startService(i);
  }
  
  private void _stopWatchingService() {
    if (false == ActivityUtil.checkServiceIsRunning(this, MainAppService.class.getName())) {
      Logger.d(TAG, "Watching Service is NOT running!");
      return;
    }
    
    Intent i = new Intent(this, com.studentpal.app.MainAppService.class);
//    i.putExtra(Event.TAGNAME_BUNDLE_PARAM, com.studentpal.app.MainAppService.CMD_STOP_WATCHING_APP);
    stopService(i);
  }
  
  private void _startDaemonService() {
    if (true) {
      ClientEngine.getInstance().getDaemonHandler().startDaemonTask();
    } else {
      ActivityUtil.startDaemonService(this);
    }
  }
  
  private void _stopDaemonService(boolean bExitDaemon) {
    if (ActivityUtil.checkServiceIsRunning(this, MainAppService.class.getName())) {
      try {
        if (bExitDaemon) {
          ClientEngine.getInstance().getDaemonHandler().exitDaemonService();
        } else {
          ClientEngine.getInstance().getDaemonHandler().stopDaemonTask();
        }
      } catch (Exception e) {
        Logger.w(TAG, e.toString());
      }
    } else {
      ActivityUtil.stopDaemonService(this);
    }
  }
  
  private void initMainSvcView() {
    tvMainSvcStatus = (TextView) findViewById(R.id.mainSvcStatus);
    tvMainSvcInfo   = (TextView) findViewById(R.id.tvMainAppSvcInfo);
    
    btnStart = (Button) findViewById(R.id.btnStart);
    btnStart.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Logger.i(TAG, btnStart.getText() + " is clicked!");

        EditText editSvrIP = (EditText) findViewById(R.id.editSvrIP);
        String svrIP = editSvrIP.getEditableText().toString().trim();
        if (Utils.isEmptyString(svrIP) == false) {
          // ClientEngine.getInstance().getIoHandler().setServerIP(svrIP);
          IoHandler.setServerIP(svrIP);
        }

        _startWatchingService();
        tvMainSvcStatus.setText("MAIN SERVICE STARTED");

//        btnStart.setClickable(false);
//        btnStop.setClickable(true);
      }
    });

    btnStop = (Button) findViewById(R.id.btnStop);
    btnStop.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Logger.i(TAG, btnStop.getText() + " is clicked!");

        _stopWatchingService();
        tvMainSvcStatus.setText("MAIN SERVICE STOPPED");

        //btnStart.setClickable(true);
        //btnStop.setClickable(false);
      }
    });
    
    //btnStart.setClickable(true);
    //btnStop.setClickable(false);
    
    btnSendAction = (Button) findViewById(R.id.btnSendAction);
    btnSendAction.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Logger.i(TAG, btnSendAction.getText() + " is clicked!");

        Intent intent = new Intent(Intent.ACTION_MAIN);
        sendBroadcast(intent);
      }
    });

    btnInstDaemon = (Button) findViewById(R.id.btnInstallDaemonApk);
    btnInstDaemon.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Logger.i(TAG, btnInstDaemon.getText() + " is clicked!");
        if (false == ActivityUtil.checkAppIsInstalled(
            view.getContext(),
            ResourceManager.DAEMON_SVC_PKG_NAME)) {
          String apkPath = ActivityUtil.getFilePathOnSdCard("/bSpalDaemon.apk");
          apkPath = "/data/bSpalDaemon.apk";
          //apkPath = "assets/SPal_ClientDaemon.apk";
          File apkFile = new File(apkPath);
          if (false == apkFile.exists()) {
            Logger.w(TAG, "Daemon APK file NOT exists!");
            return;
          }
          
          Uri apkUri = Uri.fromFile(apkFile); 
          Intent intent = new Intent(Intent.ACTION_VIEW);
          intent.setDataAndType(apkUri,"application/vnd.android.package-archive");
          startActivity(intent);
          
        } else {
          String info = "Daemon APK is already installed!";
          ActivityUtil.showToast(LaunchScreen.this, info);
          Logger.d(TAG, info);
        }
      }
    });
    
  }
  
  private void initDaemonSvcView() {
    tvDaeSvcStatus = (TextView) findViewById(R.id.daemonSvcStatus);
    tvDaeSvcInfo   = (TextView)findViewById(R.id.tvDaemonSvcInfo);
    
    btnStartDae = (Button) findViewById(R.id.btnStartDaemon);
    btnStartDae.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Logger.i(TAG, btnStartDae.getText() + " is clicked!");

        _startDaemonService();
        tvDaeSvcStatus.setText("DAEMON SERVICE STARTED");

//        btnStartDae.setClickable(false);
//        btnStopDae.setClickable(true);
      }
    });

    btnStopDae = (Button) findViewById(R.id.btnStopDaemon);
    btnStopDae.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Logger.i(TAG, btnStopDae.getText() + " is clicked!");

        _stopDaemonService(false);
        tvDaeSvcStatus.setText("DAEMON SERVICE STOPPED");

        //btnStartDae.setClickable(true);
        //btnStopDae.setClickable(false);
      }
    });
    
    btnExitDae = (Button) findViewById(R.id.btnExitDaemon);
    btnExitDae.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Logger.i(TAG, btnExitDae.getText() + " is clicked!");

        _stopDaemonService(true);
        tvDaeSvcStatus.setText("DAEMON SERVICE EXITED");

        btnStartDae.setClickable(true);
        //btnStopDae.setClickable(false);
      }
    });

    //btnStartDae.setClickable(true);
    //btnStopDae.setClickable(false);
  }
  
  
}