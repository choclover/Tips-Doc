package com.studentpal.model;

import java.util.HashMap;
import java.util.List;

import com.studentpal.model.rules.AccessRule;

public class AccessCategory {
  private String _name;
  private int    _id;
  private HashMap<String, String> _managedAppsMap;
  private List<AccessRule> _ruleList;
  
  public void addManagedApp(ClientAppInfo appInfo) {
    if (appInfo != null) {
      _managedAppsMap.put(appInfo.getAppClassname(), appInfo.getAppName());
    }
  }
  
  public void addAccessRule(AccessRule rule) {
    if (rule != null) {
      _ruleList.add(rule);
    }
  }
  
  
  
}
