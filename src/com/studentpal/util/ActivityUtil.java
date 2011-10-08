package com.studentpal.util;

import com.studentpal.app.ResourceManager;
import com.studentpal.util.logger.Logger;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class ActivityUtil {
  /*
   * Constants
   */
  private static final String TAG = "@@ ActivityUtil";
  private static final String PREFS_NAME = "com.studentpal";

  private ActivityUtil() {
  }

  /*
   * 设置全屏
   */
  public static void setFullscreen(Activity activity) {
    activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }

  /*
   * 设置没有标题
   */
  public static void setNoTitle(Activity activity) {
    activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
  }

  /*
   * 获得屏幕参数
   */
  public static DisplayMetrics getDisplayInfo(Activity activity) {
    DisplayMetrics displayMetric = activity.getResources().getDisplayMetrics();
    return displayMetric;
  }

  /*
   * 保存key:value对到preference中 key： key参数 value：key值
   */
  public static void savePreference(Context activity, String key, String value) {
    SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(key, value);
    editor.commit();
  }

  /*
   * 取得保存好的数据 key: key值 value：如果不存在时的默认值
   */
  public static String getPreference(Context activity, String key, String value) {
    SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
    String res = settings.getString(key, value);
    return res;
  }

  /*
   * 隐藏输入法
   */
  public static void setInputMethodHidden(Activity activity) {
    activity.getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
  }

  // 获取AndroidManifest.xml中android:versionName
  public static String getSoftwareVersion(Context context) {
    String packageName = PREFS_NAME;
    String result = "";
    try {
      PackageInfo pkinfo = context.getPackageManager().getPackageInfo(
          packageName, PackageManager.GET_CONFIGURATIONS);
      result = pkinfo.versionName;
    } catch (NameNotFoundException e) {
      Logger.w(TAG, e.toString());
    }
    return result;
  }

  // 获取AndroidManifest.xml中android:versionCode
  public static int getVersionCode(Context context) {
    String packageName = PREFS_NAME;
    int result = 0;
    try {
      PackageInfo pkinfo = context.getPackageManager().getPackageInfo(
          packageName, PackageManager.GET_CONFIGURATIONS);
      result = pkinfo.versionCode;
    } catch (NameNotFoundException e) {
      Logger.w(TAG, e.toString());
    }

    return result;
  }

  // 启动新Intent,带参数
  public static void directToIntent(Context context, Class<?> classname,
      String param) {
    Intent intent = new Intent(context, classname);
    if (Utils.isEmptyString(param) == false) {
      intent.putExtra("param", param);
    }
    context.startActivity(intent);
  }

  // 启动新Intent,不带参数
  public static void directToIntent(Context context, Class<?> classname) {
    directToIntent(context, classname, null);
  }

  public static void directToFinish(Context context, Class<?> classname, String param) {
    directToIntent(context, classname, param);
    ((Activity) context).finish();
  }

  public static View inflate(Activity context, int layoutId) {
    return context.getLayoutInflater().inflate(layoutId, null);
  }

  public static View inflate(Context context, int layoutId) {
    return ((Activity) context).getLayoutInflater().inflate(layoutId, null);
  }

  public static void showToast(Context context, String param) {
    Toast localToast = Toast.makeText(context, param, Toast.LENGTH_LONG);
    localToast.show();
  }

  public static void showQuitAppDialog(final Activity parent) {
    AlertDialog.Builder builder = new AlertDialog.Builder(parent);
    builder.setTitle(ResourceManager.RES_STR_QUITAPP).setMessage(
        ResourceManager.RES_STR_QUITAPP);
    builder.setPositiveButton(ResourceManager.RES_STR_OK,
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            parent.finish();
            exitApp();
          }
        }).setNegativeButton(ResourceManager.RES_STR_CANCEL, null);

    builder.create().show();
  }
  //////////////////////////////////////////////////////////////////////////////
  public static void exitApp() {
    int pid = android.os.Process.myPid();
    android.os.Process.killProcess(pid);
    System.exit(1);
  }
  
  public static void killProcess(Context context, RunningAppProcessInfo p) {
    ActivityManager activityManager = (ActivityManager) context
        .getSystemService(Context.ACTIVITY_SERVICE);
    killProcess(activityManager, p);
  }
  
  public static boolean killProcess(ActivityManager activityManager, RunningAppProcessInfo p) {
    if (activityManager==null || p==null) {
      Logger.w(TAG, "ActivityManager or ProcessInfo should NOT be NULL!");
    }
    int apiVer = android.os.Build.VERSION.SDK_INT;
    String[] pkgs = p.pkgList;
    for (String pkg : pkgs) {
      if (apiVer <= android.os.Build.VERSION_CODES.ECLAIR_MR1) {
        //for API 2.1 and earlier version
        activityManager.restartPackage(pkg);
      } else if (apiVer >= android.os.Build.VERSION_CODES.FROYO) {
        //for API 2.2 and higher version
        activityManager.killBackgroundProcesses(pkg);
      } else {
        android.os.Process.killProcess(p.pid);
        //android.os.Process.killProcess(android.os.Process.myPid());
      }
    }

    return true;
  }
  
}
