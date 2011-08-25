package studentpal.model;

import studentpal.engine.ClientEngine;
import studentpal.util.logger.Logger;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class ClientAppInfo {
  private static final String TAG = "ClientAppInfo";
  
  private String app_name;
  private String app_classname;
  private String app_pkgname;
  
  public ClientAppInfo(ApplicationInfo appInfo) {
    if (appInfo != null) {
      PackageManager pm = ClientEngine.getInstance().getPackageManager();
      if (pm == null) {
        Logger.w(TAG, "Got NULL PackageManager from engine!");
        return;
      }
      
      app_name = pm.getApplicationLabel(appInfo).toString();
      app_classname = appInfo.className;
      app_pkgname  = appInfo.packageName;
    }
  }

  public String getAppName() {
    return app_name;
  }

  public String getAppClassname() {
    return app_classname;
  }

  public String getAppPkgname() {
    return app_pkgname;
  }
}
