package com.studentpal.model;

import java.util.HashMap;

public class AccessCategory {
  private String _name;
  private int    _id;
  private HashMap<String, String> _managedAppsMap;
  
  
  public void addManagedApp(ClientAppInfo appInfo) {
    if (appInfo != null) {
      _managedAppsMap.put(appInfo.getAppClassname(), appInfo.getAppName());
    }
  }
  
  
  
}
