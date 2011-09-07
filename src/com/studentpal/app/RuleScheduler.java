package com.studentpal.app;

import com.studentpal.model.AccessCategory;
import com.studentpal.model.rules.AccessRule;

public class RuleScheduler {

  class RuleExecutor implements Runnable {
    private AccessRule _adhereRule;
    
    @Override
    public void run() {
      AccessCategory accCate = _adhereRule.getAdhereCate();
      for (ClientAppInfo appInfo : accCate.)
    }
    
  }
}
