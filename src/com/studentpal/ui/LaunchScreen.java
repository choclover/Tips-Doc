package com.studentpal.ui;

import com.studentpal.R;
import com.studentpal.util.logger.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LaunchScreen extends Activity {
  private static final String TAG = "LaunchScreen";
  
  private boolean showUI = true;
  private Button btnStart, btnStop;
  private TextView service_status;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    if (showUI) {
      setContentView(R.layout.main);
      service_status =(TextView) findViewById(R.id.service_status);
      
      btnStart = (Button) findViewById(R.id.btnStart);
      btnStart.setOnClickListener(new View.OnClickListener() {
          public void onClick(View view) {
            Logger.i(TAG, btnStart.getText()+" is clicked!");
            
            startWatchingService();
            service_status.setText("SERVICE STARTED");
          }
      });
      
      btnStop = (Button) findViewById(R.id.btnStop);
      btnStop.setOnClickListener(new View.OnClickListener() {
          public void onClick(View view) {
            Logger.i(TAG, btnStop.getText()+" is clicked!");
            
            stopWatchingService();
            service_status.setText("STOPPED");
          }
      });
      
    } else {
      startWatchingService();
    }
  }

  public void startWatchingService() {
    Intent i = new Intent(this, com.studentpal.app.MainAppService.class);
//    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    i.putExtra("command", com.studentpal.app.MainAppService.CMD_START_WATCHING_APP);
    startService(i);
  }
  
  public void stopWatchingService() {
    Intent i = new Intent(this, com.studentpal.app.MainAppService.class);
    stopService(i);
  }
  
}