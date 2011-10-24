package com.studentpal.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.studentpal.R;
import com.studentpal.app.MainAppService;
import com.studentpal.app.handler.IoHandler;
import com.studentpal.app.receiver.MyDeviceAdminReceiver;
import com.studentpal.engine.ClientEngine;
import com.studentpal.engine.Event;
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
    Logger.d(TAG, "showUI is set to: "+showUI);
    
    if (showUI) {
      setContentView(R.layout.laucher_screen);
      initMainSvcView();
      initDaemonSvcView();
      
    } else {
      _startWatchingService();
      _startDaemonService();
      
      this.finish();
    }

    //Enable the Device Administration 
    try {
      MyDeviceAdminReceiver mAdminReceiver =  new MyDeviceAdminReceiver(this);
      mAdminReceiver.enableAdmin();
    } catch (STDException e) {
      Logger.w(TAG, e.toString());
    }
    
  }

  @Override
  public void onBackPressed() {
    ActivityUtil.showQuitAppDialog(this);
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case Event.SIGNAL_TYPE_DEVICE_ADMIN_ENABLED:
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
  private void _startWatchingService() {
    if (ActivityUtil.isServiceRunning(this, MainAppService.class.getName())) {
      Logger.d(TAG, "Watching Service is already running!");
      return;
    }
    
    Intent i = new Intent(this, com.studentpal.app.MainAppService.class);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    i.putExtra(Event.TAGNAME_BUNDLE_PARAM, com.studentpal.app.MainAppService.CMD_START_WATCHING_APP);
    startService(i);
  }
  
  private void _stopWatchingService() {
    if (false == ActivityUtil.isServiceRunning(this, MainAppService.class.getName())) {
      Logger.d(TAG, "Watching Service is NOT running!");
      return;
    }
    
    Intent i = new Intent(this, com.studentpal.app.MainAppService.class);
//    i.putExtra(Event.TAGNAME_BUNDLE_PARAM, com.studentpal.app.MainAppService.CMD_STOP_WATCHING_APP);
    stopService(i);
  }
  
  private void _startDaemonService() {
    ActivityUtil.startDaemonService(this);
  }
  
  private void _stopDaemonService(boolean bExitDaemon) {
    if (ActivityUtil.isServiceRunning(this, MainAppService.class.getName())) {
      try {
        int sigType = Event.SIGNAL_TYPE_STOP_DAEMONTASK;
        if (bExitDaemon) {
          sigType = Event.SIGNAL_TYPE_EXIT_DAEMONTASK;
        }
        ClientEngine.getInstance().getDaemonHandler().sendMsgToDaemon(sigType);
        
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    } else {
      ActivityUtil.stopDaemonService(this);
    }
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
        //btnStop.setClickable(false);
      }
    });

    btnStart.setClickable(true);
    //btnStop.setClickable(false);
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

        _stopDaemonService(true);
        tvDaeSvcStatus.setText("DAEMON SERVICE STOPPED");

        btnStartDae.setClickable(true);
        //btnStopDae.setClickable(false);
      }
    });

    btnStartDae.setClickable(true);
    //btnStopDae.setClickable(false);
  }
}