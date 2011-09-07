package com.studentpal.engine;

import java.util.HashMap;

public class Event {
  private final static String TAG = "Engine.Event";
  
  public static final String MESSAGE_HEADER_ACK = "A";
  public static final String MESSAGE_HEADER_REQ = "R";
  public static final String MESSAGE_HEADER_NOTIF = "N";
  
  public static final String TAGNAME_MSG_TYPE = "msg_type";
  public static final String TAGNAME_MSG_ID   = "msg_id";
  public static final String TAGNAME_CMD_TYPE = "cmd_type";
  public static final String TAGNAME_ERR_CODE = "err_code";
  public static final String TAGNAME_RESULT    = "result";
  public static final String TAGNAME_ARGUMENTS = "args";

  public static final String TAGNAME_PHONE_NUM       = "phone_no";
  public static final String TAGNAME_APPLICATIONS    = "applications";
  public static final String TAGNAME_APP_NAME        = "app_name";
  public static final String TAGNAME_APP_CLASSNAME   = "app_classname";
  public static final String TAGNAME_APP_ACCESS_TYPE = "access_type";
  
  public final static String TASKNAME_Generic     = "Generic";
  public final static String TASKNAME_GetAppList  = "GetAppList";
  public final static String TASKNAME_SetAppAccessCategory = "SetAppAccessCategory";
  public final static String TASKNAME_SetAccessCategories = "SetAccessCategories";
  /* Tasks from Phone */
  public final static String TASKNAME_LOGIN   = "LOGIN";
  public final static String TASKNAME_LOGOUT  = "LOGOUT";
  
  public static final int MSG_ID_INVALID = -1;
  public static final int MSG_ID_NOTUSED = 0;
  
  public static final int SIGNAL_TYPE_REQACK = 101;
  public static final int SIGNAL_TYPE_OUTSTREAM_READY = 102;
  
  public static final int ERRCODE_NOERROR = 0;
  public static final int ERRCODE_TIMEOUT = 100;
  public static final int ERRCODE_FORMAT_ERR = 400;
  public static final int ERRCODE_SERVER_INTERNAL_ERR = 500;

  private static final HashMap<Integer, String> ERRCODE_DESC_MAPPER 
    = new HashMap<Integer, String>();
  
  private int type = 0;
  private int errcoe = 0;
  private Object data = null;
  
  public Event() {
  }
  
//  static {
//    ERRCODE_DESC_MAPPER.put(ERRCODE_NOERROR, "SUCCESS");
//  }
  
  public void setData(int type, int code, Object data) {
    this.type = type;
    this.errcoe = code;
    this.data = data;
  }

  public int getType() {
    return this.type;
  }

  public int getCode() {
    return this.errcoe;
  }

  public Object getData() {
    return this.data;
  }
  

}

class Signal extends Event {
  
}