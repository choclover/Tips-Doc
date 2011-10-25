package com.studentpaldaemon.test;

import android.app.Activity;
import android.os.Bundle;

import com.studentpaldaemon.app.receiver.MyDeviceAdminReceiver;
import com.studentpaldaemon.util.logger.Logger;

public class TestActivity extends Activity {
  private static final String TAG = "TestActivity";
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    try {
      MyDeviceAdminReceiver mAdminReceiver =  new MyDeviceAdminReceiver(this);
      if (false) {  //FIXME
        mAdminReceiver.enableAdmin();
      }
    } catch (Exception e) {
      Logger.w(TAG, e.toString());
    }
    
    finish();
  }
}
