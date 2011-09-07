package com.studentpal.app;

import java.util.Set;

import com.studentpal.model.AccessCategory;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.model.rules.AccessRule;

public class RuleScheduler {

  class RuleExecutor implements Runnable {
    private AccessRule _adhereRule;
    
    @Override
    public void run() {
      int denyChange = _adhereRule.isDenying() ? 1 : -1;

      AccessCategory accCate = _adhereRule.getAdhereCate();
      Set<ClientAppInfo> appInfoSet = accCate.getManagedApps().keySet();
      for (ClientAppInfo appInfo : appInfoSet) {
        accCate.adjustRestrictedRuleCnt(appInfo, denyChange);
      }
    }
    
    public void setAccessRule (AccessRule rule) {
      _adhereRule = rule;
    }
  }
}
