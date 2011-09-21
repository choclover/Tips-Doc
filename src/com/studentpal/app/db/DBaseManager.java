package com.studentpal.app.db;

import java.io.File;
import java.util.List;

import com.studentpal.engine.AppHandler;
import com.studentpal.model.AccessCategory;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.model.rules.AccessRule;
import com.studentpal.util.logger.Logger;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class DBaseManager implements AppHandler {
  /*
   * Constants
   */
  private static final String TAG = "@@ DBaseManager";
  private static final String DATABASE_ROOT = "/sdcard/studentpal/db/";
  private static final String DATABASE_NAME = "studentpal.db";
  
  private static final String ACCESS_CATEGORY_TABLE_NAME = "access_catory";
  private static final String ACCESS_RULES_TABLE_NAME = "access_rule";
  private static final String ACCESS_MANAGEDAPPS_TABLE_NAME = "access_managedapp";
  /*
   * Member fields
   */
  private static DBaseManager dataManager;
  private final SQLiteDatabase mDb;

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
    mDb = openDB();
  }

  public void launch() {
    if (mDb != null) {
      createTables(mDb);
      //mDb.close();
    } 
  }
  
  public void terminate() {
    if (mDb != null) mDb.close();
  }
  
  /*
   * 打开数据库
   */
  public synchronized SQLiteDatabase openDB() {
    SQLiteDatabase db = null;
    try {
      File file = new File(DATABASE_ROOT);
      if (!file.exists()) {
        file.mkdirs();
      }
      db = SQLiteDatabase.openDatabase(DATABASE_ROOT + DATABASE_NAME,
          null, SQLiteDatabase.OPEN_READWRITE + SQLiteDatabase.CREATE_IF_NECESSARY);
    } catch (Exception e) {
      Logger.w(TAG, e.toString());
    }
    return db;
  }

  public void saveAccessCategoriesToDB(List<AccessCategory> catesList) {
    if (catesList==null || catesList.size()==0) return;
    
    //clear old records first
//    String sqlStr = "DELETE FROM " + ACCESS_CATEGORY_TABLE_NAME;
//    mDb.execSQL(sqlStr);
    mDb.delete(ACCESS_CATEGORY_TABLE_NAME, null, null);
    
    ContentValues cv;
    for (AccessCategory aCate : catesList) {
      cv = new ContentValues();
      cv.put("cate_id", aCate.get_id());
      cv.put("cate_name", aCate.get_name());
      mDb.insert(ACCESS_CATEGORY_TABLE_NAME, null, cv);
      
      for (AccessRule rule : aCate.getAccessRules()) {
        cv = new ContentValues();
        cv.put("auth_type", rule.getAccessType());
        cv.put("repeat_type", rule.getRecurType());
        cv.put("auth_type", rule.getAccessType());
        //cv.put("start_time", rule.getAccessType());
        //cv.put("end_time", rule.getAccessType());
        cv.put("cate_id", aCate.get_id());
        //TODO
        
        mDb.insert(ACCESS_RULES_TABLE_NAME, null, cv);
      }
      
      for (ClientAppInfo appInfo : aCate.getManagedApps().keySet()) {
        cv = new ContentValues();
        cv.put("auth_type", rule.getAccessType());
        cv.put("repeat_type", rule.getRecurType());
        cv.put("auth_type", rule.getAccessType());
        //cv.put("start_time", rule.getAccessType());
        //cv.put("end_time", rule.getAccessType());
        cv.put("cate_id", aCate.get_id());
        //TODO
        
        mDb.insert(ACCESS_RULES_TABLE_NAME, null, cv);
      }
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////
  /*
   * 创建数据表
   */
  private void createTables(SQLiteDatabase dbase) {
    if (dbase == null || dbase.isOpen()==false) {
      Logger.w(TAG, "DBASE is NULL or NOT open!");
      return;
    }
    
    final String create_catory_table_sql = new StringBuffer().append( 
      "CREATE TABLE IF NOT EXISTS ").append(ACCESS_CATEGORY_TABLE_NAME).append(
      "( cate_id    INTEGER PRIMARY KEY ").append( 
      ", cate_name  TEXT").append(
      ");").toString();
    dbase.execSQL(create_catory_table_sql);
    
    final String create_rules_table_sql = new StringBuffer().append( 
      "CREATE TABLE IF NOT EXISTS ").append(ACCESS_RULES_TABLE_NAME).append(
      "(  _id           INTEGER PRIMARY KEY AUTOINCREMENT ").append( 
      ", auth_type      INTEGER").append(
      ", repeat_type    INTEGER").append(        
      ", repeat_value   INTEGER").append(
      ", start_time     TEXT").append(
      ", end_time       TEXT").append(          
      ", cate_id        INTEGER").append(        
      ", FOREIGN KEY(cate_id) REFERENCES access_catory(cate_id)").append(        
      ");").toString();
    dbase.execSQL(create_rules_table_sql);
    
    final String create_applications_table_sql = new StringBuffer().append( 
        "CREATE TABLE IF NOT EXISTS ").append(ACCESS_MANAGEDAPPS_TABLE_NAME).append(
        "( _id            INTEGER PRIMARY KEY AUTOINCREMENT ").append(
        ", app_name       INTEGER").append(
        ", app_pgkname    INTEGER").append(
        ", app_classname  INTEGER").append(
        ", cate_id        INTEGER").append(
        ", FOREIGN KEY(cate_id) REFERENCES access_catory(cate_id)").append(
        ");").toString();
    dbase.execSQL(create_applications_table_sql);
    
    
  }
}

