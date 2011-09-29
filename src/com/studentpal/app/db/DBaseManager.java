package com.studentpal.app.db;

import static com.studentpal.engine.Event.TAGNAME_ACCESS_CATE_ID;
import static com.studentpal.engine.Event.TAGNAME_ACCESS_CATE_NAME;
import static com.studentpal.engine.Event.TAGNAME_APP_CLASSNAME;
import static com.studentpal.engine.Event.TAGNAME_APP_NAME;
import static com.studentpal.engine.Event.TAGNAME_APP_PKGNAME;
import static com.studentpal.engine.Event.TAGNAME_RULE_AUTH_TYPE;
import static com.studentpal.engine.Event.TAGNAME_RULE_REPEAT_ENDTIME;
import static com.studentpal.engine.Event.TAGNAME_RULE_REPEAT_STARTTIME;
import static com.studentpal.engine.Event.TAGNAME_RULE_REPEAT_TYPE;
import static com.studentpal.engine.Event.TAGNAME_RULE_REPEAT_VALUE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.studentpal.engine.AppHandler;
import com.studentpal.engine.ClientEngine;
import com.studentpal.model.AccessCategory;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.model.exception.STDException;
import com.studentpal.model.rules.AccessRule;
import com.studentpal.model.rules.Recurrence;
import com.studentpal.model.rules.TimeRange;
import com.studentpal.util.logger.Logger;

public class DBaseManager /*implements AppHandler*/ {
  /*
   * Constants
   */
  private static final String TAG = "@@ DBaseManager";
//  private static final String DATABASE_ROOT = "/mnt/sdcard/studentpal/db/";
  private static final String DATABASE_NAME = "studentpal.db";
  
  private static final String  TABLE_NAME_ACCESS_CATEGORIES  = "access_categories";
  private static final String  TABLE_NAME_ACCESS_RULES       = "access_rules";
  private static final String  TABLE_NAME_ACCESS_MANAGEDAPPS = "access_managedapps";
  
  private static final String  TIME_LIST_DELIMETER = ";";
  
  /*
   * Member fields
   */
  private static DBaseManager dataManager;
  private SQLiteDatabase mDb;
  private File mDbFile;

  /*
   * 获取实例，应当在欢迎界面之后建立实例
   */
  public static DBaseManager getInstance() {
    if (dataManager == null) {
      dataManager = new DBaseManager();
    }
    return dataManager;
  }

  private DBaseManager() {
    initialize();
  }

  /////////////////////////////////////////////////////////////////////////////
  public void saveAccessCategoriesToDB(List<AccessCategory> catesList) {
    if (catesList==null || catesList.size()==0) return;
    Logger.i(TAG, "enter saveAccessCategoriesToDB()!");
    
    mDb = openDB();
    
    //clear old records first
    //String sqlStr = "DELETE FROM " + ACCESS_CATEGORY_TABLE_NAME;
    //mDb.execSQL(sqlStr);
    long res = -1;
    res = mDb.delete(TABLE_NAME_ACCESS_CATEGORIES, "1", null);
    res = mDb.delete(TABLE_NAME_ACCESS_RULES, "1", null);
    res = mDb.delete(TABLE_NAME_ACCESS_MANAGEDAPPS, "1", null);
    
    ContentValues cv;
    for (AccessCategory aCate : catesList) {
      cv = new ContentValues();
      cv.put(TAGNAME_ACCESS_CATE_ID,    aCate.get_id());
      cv.put(TAGNAME_ACCESS_CATE_NAME,  aCate.get_name());
      res = mDb.insert(TABLE_NAME_ACCESS_CATEGORIES, null, cv);
      
      //Insert access rules records
      for (AccessRule rule : aCate.getAccessRules()) {
        cv = new ContentValues();
        cv.put(TAGNAME_RULE_AUTH_TYPE, rule.getAccessType());
        cv.put(TAGNAME_RULE_REPEAT_TYPE, rule.getRecurType());
        cv.put(TAGNAME_RULE_REPEAT_VALUE, rule.getRecurrence().toString());
        
        String startTimeStr = "";
        String endTimeStr = "";
        for (TimeRange tr : rule.getTimeRangeList()) {
          startTimeStr += tr.getStartTime().toString() + TIME_LIST_DELIMETER;
          endTimeStr += tr.getEndTime().toString() + TIME_LIST_DELIMETER;
        }
        cv.put(TAGNAME_RULE_REPEAT_STARTTIME, startTimeStr);
        cv.put(TAGNAME_RULE_REPEAT_ENDTIME, endTimeStr);
        cv.put(TAGNAME_ACCESS_CATE_ID, aCate.get_id());
        
        res = mDb.insert(TABLE_NAME_ACCESS_RULES, null, cv);
      }
      
      //Insert access managed apps records
      for (ClientAppInfo appInfo : aCate.getManagedApps().keySet()) {
        cv = new ContentValues();
        cv.put(TAGNAME_APP_NAME,      appInfo.getAppName());
        cv.put(TAGNAME_APP_PKGNAME,   appInfo.getAppPkgname());
        cv.put(TAGNAME_APP_CLASSNAME, appInfo.getAppClassname());
        cv.put(TAGNAME_ACCESS_CATE_ID, aCate.get_id());

        res = mDb.insert(TABLE_NAME_ACCESS_MANAGEDAPPS, null, cv);
      }
    }
    
    mDb.close();
  }
  
  public List<AccessCategory> loadAccessCategoriesFromDB() throws STDException {
    List<AccessCategory> catesList = new ArrayList<AccessCategory>();
    mDb = openDB();

    Cursor curCate = mDb.query(TABLE_NAME_ACCESS_CATEGORIES, null, null, null, null, null, null);
    while (curCate.moveToNext()) {
      AccessCategory aCate = new AccessCategory();
      
      int startIdx = 0;
      int cate_id = curCate.getInt(startIdx++);  //0
      aCate.set_id(cate_id);
      aCate.set_name(curCate.getString(startIdx++));  //1

      Cursor curRule = mDb.query(TABLE_NAME_ACCESS_RULES, null,
          TAGNAME_ACCESS_CATE_ID + "=" + aCate.get_id(), null, null, null, null);
      while (curRule.moveToNext()) {
        AccessRule aRule = new AccessRule();
        
        startIdx = 1;
        aRule.setAccessType(curRule.getInt(startIdx++));  //1
        Recurrence recur = Recurrence.getInstance(curRule.getInt(startIdx++));  //2
        if (recur.getRecurType() == Recurrence.DAILY) {
          startIdx++;
          recur.setRecurValue(0);
        } else {
          recur.setRecurValue(curRule.getInt(startIdx++));  //3
        }
        aRule.setRecurrence(recur);
        
        String timeStr = curRule.getString(startIdx++);
        StringTokenizer startTokens = new StringTokenizer(timeStr,  //4
            TIME_LIST_DELIMETER);
        timeStr = curRule.getString(startIdx++);
        StringTokenizer endTokens = new StringTokenizer(timeStr,  //5
            TIME_LIST_DELIMETER);
        while (startTokens.hasMoreTokens()) {
          TimeRange tr = new TimeRange();
          
          timeStr = startTokens.nextToken();
          tr.setTime(TimeRange.TIME_TYPE_START, timeStr);
          
          timeStr = endTokens.nextToken();
          tr.setTime(TimeRange.TIME_TYPE_END, timeStr);
          
          aRule.addTimeRange(tr);
        }//for time_ranges
        
        aCate.addAccessRule(aRule);
        
      }//while curRule
      curRule.close();
      
      Cursor curApp = mDb.query(TABLE_NAME_ACCESS_MANAGEDAPPS, null,
          TAGNAME_ACCESS_CATE_ID + "=" + aCate.get_id(), null, null, null, null);
      while (curApp.moveToNext()) {
        startIdx = 1;
        String appName = curApp.getString(startIdx++);    //1
        String pkgName = curApp.getString(startIdx++);    //2
        String className = curApp.getString(startIdx++);  //3
        ClientAppInfo appInfo = new ClientAppInfo(appName, pkgName, className);
        aCate.addManagedApp(appInfo);
        
      }//while curApp
      curApp.close();
      
      catesList.add(aCate);
      
    }//while curCate
    curCate.close();
    
    mDb.close();
    
    return catesList;
  }
  
  //////////////////////////////////////////////////////////////////////////////
  private String getDatabaseRoot() {
    String result = "";
    
//    int apiVer = android.os.Build.VERSION.SDK_INT;
//    if (apiVer <= android.os.Build.VERSION_CODES.ECLAIR_MR1) {
//      // for API 2.1 and earlier version
//      result = "/sdcard/studentpal/db/";
//    } else if (apiVer >= android.os.Build.VERSION_CODES.FROYO) {
//      // for API 2.2 and higher version
//      result = "/mnt/sdcard/studentpal/db/";
//    }
//    result = Environment.getDataDirectory().toString();
  
    result = getDbFilePath().getParent();
    
    return result;
  }
  
  private void initialize() {
    mDbFile = getDbFilePath();
    File dbFolder = mDbFile.getParentFile();
    if (!dbFolder.exists()) {
      dbFolder.mkdirs();
    }
    
    mDb = openDB();
    if (mDb != null) {
      createTables(mDb);
      mDb.close();
    }
  }
  
  private File getDbFilePath() {
    File result = null;
    result = ClientEngine.getInstance().getContext().
                    getApplicationContext().getDatabasePath(DATABASE_NAME);
    
    return result;
  }
  
  /*
   * 打开数据库
   */
  private synchronized SQLiteDatabase openDB() {
    SQLiteDatabase db = null;
    
    try {
      db = SQLiteDatabase.openDatabase(this.mDbFile.getAbsolutePath(),
          null, SQLiteDatabase.OPEN_READWRITE + SQLiteDatabase.CREATE_IF_NECESSARY);

    } catch (Exception e) {
      Logger.w(TAG, e.toString());
    }
    return db;
  }
  
  /*
   * 创建数据表
   */
  private void createTables(SQLiteDatabase dbase) {
    if (dbase == null || dbase.isOpen()==false) {
      Logger.w(TAG, "DBASE is NULL or NOT open!");
      return;
    }
    
    final String create_catory_table_sql = new StringBuffer().append(
      "CREATE TABLE IF NOT EXISTS ").append(TABLE_NAME_ACCESS_CATEGORIES).append(
      "( " +TAGNAME_ACCESS_CATE_ID+   " INTEGER PRIMARY KEY ").append(
      ", " +TAGNAME_ACCESS_CATE_NAME+ " TEXT").append(
      ");").toString();
    dbase.execSQL(create_catory_table_sql);
    
    final String create_rules_table_sql = new StringBuffer().append(
      "CREATE TABLE IF NOT EXISTS ").append(TABLE_NAME_ACCESS_RULES).append(
      "(  _id INTEGER PRIMARY KEY AUTOINCREMENT ").append(
      ", " +TAGNAME_RULE_AUTH_TYPE+        " INTEGER").append(
      ", " +TAGNAME_RULE_REPEAT_TYPE+      " INTEGER").append(
      ", " +TAGNAME_RULE_REPEAT_VALUE+     " INTEGER").append(
      ", " +TAGNAME_RULE_REPEAT_STARTTIME+ " TEXT").append(
      ", " +TAGNAME_RULE_REPEAT_ENDTIME+   " TEXT").append(
      ", " +TAGNAME_ACCESS_CATE_ID+        " INTEGER").append(
      ", FOREIGN KEY(" +TAGNAME_ACCESS_CATE_ID+ ") REFERENCES " +TABLE_NAME_ACCESS_CATEGORIES+ "(" +TAGNAME_ACCESS_CATE_ID+ ")").append(
      ");").toString();
    dbase.execSQL(create_rules_table_sql);
    
    final String create_applications_table_sql = new StringBuffer().append(
        "CREATE TABLE IF NOT EXISTS ").append(TABLE_NAME_ACCESS_MANAGEDAPPS).append(
        "( _id INTEGER PRIMARY KEY AUTOINCREMENT ").append(
        ", " +TAGNAME_APP_NAME+       " TEXT").append(
        ", " +TAGNAME_APP_PKGNAME+    " TEXT").append(
        ", " +TAGNAME_APP_CLASSNAME+  " TEXT").append(
        ", " +TAGNAME_ACCESS_CATE_ID+ " INTEGER").append(
        ", FOREIGN KEY(" +TAGNAME_ACCESS_CATE_ID+ ") REFERENCES " +TABLE_NAME_ACCESS_CATEGORIES+ "(" +TAGNAME_ACCESS_CATE_ID+ ")").append(
        ");").toString();
    dbase.execSQL(create_applications_table_sql);
  }
}

