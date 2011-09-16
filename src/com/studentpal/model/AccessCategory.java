package com.studentpal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.studentpal.app.RuleScheduler;
import com.studentpal.model.rules.AccessRule;
import com.studentpal.util.logger.Logger;

public class AccessCategory {
  private static final String TAG = "@@ AccessCategory";

  /*
   * Member fields
   */
  private String _name;
  private int _id;
  private RuleScheduler scheduler;

  /*
   * ClientAppInfo -- An AppInfo instance managed by this category Integer --
   * Count of Restricted Rules upon this ClientAppInfo
   */
  private HashMap<ClientAppInfo, Integer> _managedAppsMap;
  private List<AccessRule> _rulesList;

  public AccessCategory() {
    _managedAppsMap = new HashMap<ClientAppInfo, Integer>();
    _rulesList = new ArrayList<AccessRule>();
  }

  public void addManagedApp(ClientAppInfo appInfo) {
    if (appInfo != null) {
      synchronized (_managedAppsMap) {
        _managedAppsMap.put(appInfo, 0); // init the restrictedRuleCnt to 0
      }
    }
  }

  public void removeManagedApp(ClientAppInfo appInfo) {
    if (appInfo != null) {
      synchronized (_managedAppsMap) {
        if (null == _managedAppsMap.remove(appInfo)) {
          Logger.w(TAG, "_managedAppsMap return NULL for AppInfo "
              + appInfo.getAppClassname());
        }
      }
    } else {
      Logger.w(TAG, "Input parameter ClientAppInfo is NULL!");
    }
  }

  public void addAccessRule(AccessRule rule) {
    if (rule != null) {
      rule.setAdhereCategory(this);
      _rulesList.add(rule);
    }
  }

  public List<AccessRule> getAccessRules() {
    return _rulesList;
  }

  public void clearRules() {
    if (_rulesList != null)
      _rulesList.clear();
  }

  public HashMap<ClientAppInfo, Integer> getManagedApps() {
    return _managedAppsMap;
  }

  public RuleScheduler getScheduler() {
    if (scheduler == null) {
      scheduler = new RuleScheduler();
    }
    return scheduler;
  }

  public void adjustRestrictedRuleCount(ClientAppInfo appInfo, int delta) {
    if (appInfo == null || delta == 0)
      return;
    
    if (_managedAppsMap.containsKey(appInfo) == false) {
      Logger.w(TAG, "_managedAppsMap NOT contains AppInfo: " + appInfo.getAppName());
    }
    
    synchronized (_managedAppsMap) {
      Integer oldCnt = _managedAppsMap.get(appInfo);
      Integer newCnt = oldCnt + delta;
      if (newCnt < 0) {
        newCnt = 0;
      }
      Logger.v(TAG, "Putting counter " + newCnt
          + " to _managedAppsMap for AppInfo: " + appInfo.getAppName());
      _managedAppsMap.put(appInfo, newCnt);
    }
  }

  public boolean isAccessPermitted(ClientAppInfo appInfo) {
    Integer restrictedRuleCount = _managedAppsMap.get(appInfo);
    boolean result = false;
    if (restrictedRuleCount != null) {
      result = (restrictedRuleCount == 0);
    } else {
      Logger.w(TAG, appInfo.getIndexingKey()
          + " is NOT managed by this category.");
    }
    return result;
  }
  
  public boolean isAccessDenied(ClientAppInfo appInfo) {
    return ! isAccessPermitted(appInfo);
  }

  public String get_name() {
    return _name;
  }

  public void set_name(String name) {
    _name = name;
  }

  public int get_id() {
    return _id;
  }

  public void set_id(int id) {
    _id = id;
  }

}
