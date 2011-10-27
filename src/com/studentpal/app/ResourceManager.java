package com.studentpal.app;

public class ResourceManager {
  //should be identical to value of "package" in Manifest
  public static final String APPLICATION_PKG_NAME = "com.studentpal"; 
  public static final String DAEMON_SVC_PKG_NAME = "com.studentpaldaemon";
  public static final String ACTIVITY_NAME_MANAGEAPPS  = "com.android.settings.ManageApplications";
  
  public static final String RES_STR_OK = "\u786e\u5b9a"; // 确定
  public static final String RES_STR_CANCEL = "\u53d6\u6d88"; // 取消

  public static final String RES_STR_OPERATION_DENIED = 
    "\u5f88\u62b1\u6b49\uff0c\u60a8\u7684\u64cd\u4f5c\u88ab\u7ba1\u7406\u5458\u6240\u7981\u6b62\uff01"; // 很抱歉，您的操作被管理员所禁止！
  public static final String RES_STR_SENDREQUEST = "\u53d1\u9001\u8bf7\u6c42"; // 发送请求

  public static final String RES_STR_TIME = "\u65f6\u95f4"; // 时间
  public static final String RES_STR_START_TIME = "\u5f00\u59cb" + RES_STR_TIME; // 开始时间
  public static final String RES_STR_END_TIME = "\u7ed3\u675f" + RES_STR_TIME; // 结束时间

  public static final String RES_STR_QUITAPP = "\u9000\u51fa\u7a0b\u5e8f\uff1f";  //退出程序？
  
}
