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
  /*
   * An ClientAppInfo instance managed by this category
   * Restricted Rules count upon this ClientAppInfo
   */
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
  
  public HashMap<ClientAppInfo, Integer> getManagedApps() {
    return _managedAppsMap;
  }
  
  public void adjustRestrictedRuleCnt(ClientAppInfo appInfo, int change) {
    if (appInfo==null || change==0) return;
    
    Integer oldCnt = _managedAppsMap.get(appInfo);
    Integer newCnt = oldCnt + change;
    if (oldCnt + change < 0) {
      newCnt = 0;
    }
    _managedAppsMap.put(appInfo, newCnt);
  }
  
  
  
}
