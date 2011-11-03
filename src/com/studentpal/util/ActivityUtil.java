package com.studentpal.util;

import java.util.List;

import com.studentpal.app.ResourceManager;
import com.studentpal.app.handler.DaemonHandler;
import com.studentpal.app.receiver.MyDeviceAdminReceiver;
import com.studentpal.engine.Event;
import com.studentpal.ui.LaunchScreen;
import com.studentpal.util.logger.Logger;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
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
  private static final String PREFS_NAME = ResourceManager.APPLICATION_PKG_NAME;

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
      PackageInfo pkginfo = context.getPackageManager().getPackageInfo(
          packageName, PackageManager.GET_CONFIGURATIONS);
      result = pkginfo.versionName;
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
      PackageInfo pkginfo = context.getPackageManager().getPackageInfo(
          packageName, PackageManager.GET_CONFIGURATIONS);
      result = pkginfo.versionCode;
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
    Toast localToast = Toast.makeText(context, param, Toast.LENGTH_SHORT);
    localToast.show();
  }

  public static void showToastLong(Context context, String param) {
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
  public static String getTopActivityName(Object app) {
    String result = "";
    if (app instanceof RunningAppProcessInfo) {
      result = ((RunningAppProcessInfo)app).processName;
    } else if (app instanceof RunningTaskInfo) {
      result = ((RunningTaskInfo)app).topActivity.getPackageName();
    }
    
    return result;
  }
  
  public static String getTopActivityClassName(Object app) {
    String result = "";
    if (app instanceof RunningAppProcessInfo) {
      result = ((RunningAppProcessInfo)app).processName;
    } else if (app instanceof RunningTaskInfo) {
      result = ((RunningTaskInfo)app).topActivity.getClassName();
    }
    
    return result;
  }
  
  public static String getFilePathOnSdCard(String rFilePath) {
    if (rFilePath == null) {
      Logger.w(TAG, "Input file path parameter is NULL!");
      return null;
    }
    
    String path = Environment.getExternalStorageDirectory().toString()
        +'/'+ rFilePath;
    return path;
  }
  
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
  
  public static boolean killProcess(ActivityManager activityManager,
      RunningAppProcessInfo p) {
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
  
  public static void killServiceById(Context context, int pid) {
    if (pid > 0) {
      android.os.Process.killProcess(pid);
    } else {
      Logger.d(TAG, "Invalid PID of "+pid);
    }
  }
  
  public static void startDaemonService(Context context) {
    Intent i = new Intent();
    i.setAction(Event.ACTION_DAEMON_SVC);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    
    Object result = context.startService(i);
    if (result != null) {
      Logger.d(TAG, "Starting daemon service OK!");
    } else {
      Logger.d(TAG, "Starting daemon service FAIL!");
    }
  }
  
  public static void stopDaemonService(Context context) {
    Intent i = new Intent();
    i.setAction(Event.ACTION_DAEMON_SVC);
    Logger.d(TAG, "Ready to stop daemon service!");
    
    boolean succ = context.stopService(i);
    if (succ) {
      Logger.d(TAG, "Stopping daemon service OK!");
    } else {
      Logger.d(TAG, "Stopping daemon service FAIL!");
    }
  }

  /*
   * 判断应用是否已经安装.
   * @param context
   * @param className 判断的服务的class name
   */
  public static boolean checkAppIsInstalled(Context context, String pkgName) {
    if (context==null || Utils.isEmptyString(pkgName)) {
      Logger.w(TAG, "Context is NULL or Service name is empty!");
      return false;
    }

    boolean isInstalled = false;
    try {
      ApplicationInfo info = context.getPackageManager().getApplicationInfo(
          pkgName, PackageManager.GET_UNINSTALLED_PACKAGES);
      if (info != null) {
        isInstalled = true;
      }
    } catch (NameNotFoundException e) {
      Logger.w(TAG, e.toString());
    }
    
    return isInstalled;
  }
  
  /*
   * 判断服务是否运行.
   * @param context
   * @param className 判断的服务的class name
   */
  public static boolean checkServiceIsRunning(Context mContext, String svcClsName) {
    boolean isRunning = false;
    if (null != findRunningService(mContext, svcClsName)) {
      isRunning = true;
    }
    return isRunning;
  }
  
  
  public static RunningServiceInfo findRunningService(
      Context mContext, String svcClsName) {
    if (mContext==null || Utils.isEmptyString(svcClsName)) {
      Logger.w(TAG, "Context is NULL or Service name is empty!");
      return null;
    }
    
    RunningServiceInfo result = null;
    
    ActivityManager activityManager = (ActivityManager) mContext
        .getSystemService(Context.ACTIVITY_SERVICE);
    List<RunningServiceInfo> serviceList = activityManager
        .getRunningServices(30);

    for (int i=0; i<serviceList.size(); i++) {
      RunningServiceInfo tmpSvc = serviceList.get(i); 
      if (tmpSvc != null 
          && tmpSvc.service.getClassName().equals(svcClsName)) {
        result = tmpSvc;
        break;
      }
    }
    return result;
  }
  
  public static RunningAppProcessInfo findRunningAppProcess(
      Context mContext, String classname) {
    ActivityManager activityManager = (ActivityManager) mContext
        .getSystemService(Context.ACTIVITY_SERVICE);
    
    return findRunningAppProcess(activityManager, classname);
  }
  
  public static RunningAppProcessInfo findRunningAppProcess(
      ActivityManager activityManager, String classname) {
    RunningAppProcessInfo result = null;

    List<RunningAppProcessInfo> processes = activityManager
        .getRunningAppProcesses();
    for (RunningAppProcessInfo process : processes) {
      String pname = process.processName;
      // Logger..d(TAG, pname);
      if (classname.equals(pname)) {
        result = process;
        break;
      }
    }
    return result;
  }
  
  
}//class ActivityUtil
  
  
  
