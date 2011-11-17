package com.studentpal.util.logger;

public class Logger {

  public final static String LOG_TAG = "studentpal";

  private static boolean printLog = true;

  /*
   * Set if we will print the log
   */
  public void setPrintLog(boolean value) {
    printLog = value;
  }

  public static void e(String tag, String log) {
    if (printLog)
      android.util.Log.e(tag, log);
  }

  public static void e(String log) {
    e(LOG_TAG, log);
  }
  
  public static void i(String tag, String log) {
    if (printLog)
      android.util.Log.i(tag, log);
  }

  public static void i(String log) {
    i(LOG_TAG, log);
  }
  
  public static void v(String tag, String log) {
    if (printLog)
      android.util.Log.v(tag, log);
  }

  public static void v(String log) {
    v(LOG_TAG, log);
  }
  
  public static void w(String tag, String log) {
    if (printLog)
      android.util.Log.w(tag, log);
  }
  
  public static void w(String log) {
    w(LOG_TAG, log);
  }

  public static void d(String tag, String log) {
    if (printLog)
      android.util.Log.d(tag, log);
  }
  
  public static void d(String log) {
    d(LOG_TAG, log);
  }

}
