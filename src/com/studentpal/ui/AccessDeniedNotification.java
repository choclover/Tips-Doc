package com.studentpal.ui;

import com.studentpal.app.ResourceManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class AccessDeniedNotification extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(ResourceManager.RES_STR_OPERATION_DENIED);
    builder.setCancelable(false);
    builder.setPositiveButton(ResourceManager.RES_STR_SENDREQUEST, 
      new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          AccessDeniedNotification.this.finish();
          
          Intent i = new Intent(AccessDeniedNotification.this, 
              AccessRequestForm.class);
          i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(i);
        }
    });
    builder.setNegativeButton(ResourceManager.RES_STR_CANCEL, 
      new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          dialog.cancel();
          AccessDeniedNotification.this.finish();
        }
    });
    
    AlertDialog alert = builder.create();
    alert.show();
  }
}
