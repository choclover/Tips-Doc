package com.studentpal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.studentpal.model.rules.AccessRule;
import com.studentpal.util.logger.Logger;

public class AccessCategory {
  private static final String TAG = "@@ AccessCategory";
  
  private String _name;
  private int    _id;
  private HashMap<ClientAppInfo, Integer> _managedAppsMap;
  private List<AccessRule> _ruleList;
  
  public AccessCategory() {
    _managedAppsMap = new HashMap<ClientAppInfo, Integer>();
    _ruleList = new ArrayList<AccessRule>();
  }
  
  public void addManagedApp(ClientAppInfo appInfo) {
    if (appInfo != null) {
      synchronized (_managedAppsMap) {
        _managedAppsMap.put(appInfo, 0);  //init the restrictedRuleCnt to 0
      }
    }
  }
  
  public void removeManagedApp(ClientAppInfo appInfo) {
    if (appInfo != null) {
      synchronized (_managedAppsMap) {
        if (null == _managedAppsMap.remove(appInfo.getAppClassname()) ) {
          Logger.w(TAG, "_managedAppsMap return NULL for class "+appInfo.getAppClassname());
        }
      }
    } else {
      Logger.w(TAG, "Input parameter ClientAppInfo is NULL!");
    }
  }
  
  public void addAccessRule(AccessRule rule) {
    if (rule != null) {
      _ruleList.add(rule);
    }
  }
  
  
  
}
