package com.studentpal.app.receiver;

import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.studentpal.R;
import com.studentpal.engine.Event;
import com.studentpal.model.exception.STDException;
import com.studentpal.util.logger.Logger;

/**
 * Example of a do-nothing admin class. When enabled, it lets you control some
 * of its policy and reports when there is interesting activity.
 */
public class MyDeviceAdminReceiver extends DeviceAdminReceiver {
  private static final String TAG = "MyDeviceAdminReceiver";
  
  /*
   * Member fields
   */
  private Activity            context          = null;
  private ComponentName       mDeviceAdminInst = null;
  private DevicePolicyManager mDPM             = null;
  
  /*
   * Methods
   */
  public MyDeviceAdminReceiver() {
  }
  
  public MyDeviceAdminReceiver(Activity context) throws STDException {
    if (context == null) {
      throw new STDException("Context param should NOT be NULL!");
    }
    
    this.context = context;
    this.mDPM = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    
  }
  
  @Override
  public void onEnabled(Context context, Intent intent) {
    Logger.d(TAG, "Device Admin is Enabled!");
  }

  @Override
  public CharSequence onDisableRequested(Context context, Intent intent) {
    return context.getResources().getString(R.string.device_admin_deactivated_warning);
  }

  @Override
  public void onDisabled(Context context, Intent intent) {
    Logger.d(TAG, "Device Admin is Disabled!");
  }

//  @Override
//  public void onPasswordChanged(Context context, Intent intent) {
//    Logger.d(TAG, "Sample Device Admin: pw changed");
//  }
//
//  @Override
//  public void onPasswordFailed(Context context, Intent intent) {
//    Logger.d(TAG, "Sample Device Admin: pw failed");
//  }
//
//  @Override
//  public void onPasswordSucceeded(Context context, Intent intent) {
//    Logger.d(TAG, "Sample Device Admin: pw succeeded");
//  }

  // //////////////////////////////////////////////////////////////////////////
  public void enableAdmin() {
    if (mDeviceAdminInst == null) {
      mDeviceAdminInst = new ComponentName(context, MyDeviceAdminReceiver.class);
    }
    
    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminInst);
//    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//            "Additional text explaining why this needs to be added.");
    context.startActivityForResult(intent, Event.SIGNAL_TYPE_DEVICE_ADMIN_ENABLED);
  }

  public void disableAdmin() {
    if (mDeviceAdminInst != null) {
      mDPM.removeActiveAdmin(mDeviceAdminInst);
    } else {
      Logger.w(TAG, "Device Admin instance should NOT be NULL!");
    }
  }
  
  // //////////////////////////////////////////////////////////////////////////
  
}
