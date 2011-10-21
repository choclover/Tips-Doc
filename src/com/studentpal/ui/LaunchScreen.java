package com.studentpal.ui;

import android.app.Activity;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.studentpal.R;
import com.studentpal.app.MainAppService;
import com.studentpal.app.handler.DaemonHandler;
import com.studentpal.app.handler.IoHandler;
import com.studentpal.app.receiver.MyDeviceAdminReceiver;
import com.studentpal.engine.Event;
import com.studentpal.util.ActivityUtil;
import com.studentpal.util.Utils;
import com.studentpal.util.logger.Logger;

public class LaunchScreen extends Activity {
  private static final String TAG = "LaunchScreen";
  
  /* 
   * Constants
   */
  private static final int RESULT_DEVICE_ADMIN_ENABLE = 1;
  private static boolean showUI = true;
  
  /*
   * Member fields
   */
  private Button btnStart, btnStop;
  private TextView tvMainSvcStatus;
  
  private Button btnStartDae, btnStopDae;
  private TextView tvDaeSvcStatus;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle cfgParams = getIntent().getExtras();
    if (cfgParams != null) {
      showUI = cfgParams.getBoolean(Event.CFG_SHOW_LAUNCHER_UI, showUI) ;
    }
    
    if (showUI) {
      setContentView(R.layout.laucher_screen);
      
      initMainSvcView();
      initDaemonSvcView();
      
    } else {
      if (false == ActivityUtil.isServiceRunning(this,
          MainAppService.class.getName())) {
        _startWatchingService();
      }
      
      _startDaemonService();
      
      this.finish();
    }

    //Enable the AppDeviceAdmin 
    _setAppDeviceAdmin(true);
  }

  @Override
  public void onBackPressed() {
    ActivityUtil.showQuitAppDialog(this);
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case RESULT_DEVICE_ADMIN_ENABLE:
      if (resultCode == Activity.RESULT_OK) {
        Logger.i("DeviceAdminSample", "Enable Admin OK!");
      } else {
        Logger.i("DeviceAdminSample", "Enable Admin FAILED!");
      }
      return;
    }

    super.onActivityResult(requestCode, resultCode, data);
  }
  
  //////////////////////////////////////////////////////////////////////////////
  private void _startWatchingService() {
    Intent i = new Intent(this, com.studentpal.app.MainAppService.class);
//    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    i.putExtra(Event.TAGNAME_BUNDLE_PARAM, com.studentpal.app.MainAppService.CMD_START_WATCHING_APP);
    startService(i);
  }
  
  private void _stopWatchingService() {
    Intent i = new Intent(this, com.studentpal.app.MainAppService.class);
//    i.putExtra(Event.TAGNAME_BUNDLE_PARAM, com.studentpal.app.MainAppService.CMD_STOP_WATCHING_APP);
    stopService(i);
  }
  
  private void _startDaemonService() {
    Intent i = new Intent();
    i.setAction(DaemonHandler.ACTION_DAEMON_SVC);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startService(i);
  }
  
  private void _stopDaemonService() {
    //Intent i = new Intent(this, com.studentpaldaemon.app.DaemonService.class);
    //i.setAction(DaemonHandler.ACTION_DAEMON_SVC);
    //stopService(i);
    
    String svcClsName = "com.studentpaldaemon.app.DaemonService";
    RunningServiceInfo svcInfo = ActivityUtil.findRunningService(this, svcClsName); 
    if (svcInfo != null ) {
      Logger.d("Daemon service is running, to kill it...");
      ActivityUtil.killServiceById(this, svcInfo.pid);
    } else {
      Logger.d("Daemon service is NOT running!");
    }
  }
  
  private void _setAppDeviceAdmin(boolean active) {
    ComponentName mDeviceAdminInst = new ComponentName(LaunchScreen.this,
        MyDeviceAdminReceiver.class);
    
    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
        mDeviceAdminInst);
    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Additional text explaining why this needs to be added.");//FIXME
    startActivityForResult(intent, RESULT_DEVICE_ADMIN_ENABLE);
  }
  
  private void initMainSvcView() {
    tvMainSvcStatus = (TextView) findViewById(R.id.mainSvcStatus);

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

        btnStart.setClickable(false);
        btnStop.setClickable(true);
      }
    });

    btnStop = (Button) findViewById(R.id.btnStop);
    btnStop.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Logger.i(TAG, btnStop.getText() + " is clicked!");

        _stopWatchingService();
        tvMainSvcStatus.setText("MAIN SERVICE STOPPED");

        btnStart.setClickable(true);
        btnStop.setClickable(false);
      }
    });

    btnStart.setClickable(true);
    btnStop.setClickable(false);
  }
  
  private void initDaemonSvcView() {
    tvDaeSvcStatus = (TextView) findViewById(R.id.daemonSvcStatus);

    btnStartDae = (Button) findViewById(R.id.btnStartDaemon);
    btnStartDae.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Logger.i(TAG, btnStartDae.getText() + " is clicked!");

        _startDaemonService();
        tvDaeSvcStatus.setText("DAEMON SERVICE STARTED");

        btnStartDae.setClickable(false);
        btnStopDae.setClickable(true);
      }
    });

    btnStopDae = (Button) findViewById(R.id.btnStopDaemon);
    btnStopDae.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        Logger.i(TAG, btnStopDae.getText() + " is clicked!");

        _stopDaemonService();
        tvDaeSvcStatus.setText("DAEMON SERVICE STOPPED");

        btnStartDae.setClickable(true);
        btnStopDae.setClickable(false);
      }
    });

    btnStartDae.setClickable(true);
    btnStopDae.setClickable(false);
    
  }
}