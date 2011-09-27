package com.studentpal.engine.request;

import static com.studentpal.engine.Event.ERRCODE_MSG_FORMAT_ERR;
import static com.studentpal.engine.Event.ERRCODE_NOERROR;
import static com.studentpal.engine.Event.TAGNAME_ACCESS_CATEGORIES;
import static com.studentpal.engine.Event.TAGNAME_ACCESS_CATE_ID;
import static com.studentpal.engine.Event.TAGNAME_ACCESS_CATE_NAME;
import static com.studentpal.engine.Event.TAGNAME_ACCESS_RULES;
import static com.studentpal.engine.Event.TAGNAME_ACCESS_TIMERANGES;
import static com.studentpal.engine.Event.TAGNAME_APPLICATIONS;
import static com.studentpal.engine.Event.TAGNAME_APP_CLASSNAME;
import static com.studentpal.engine.Event.TAGNAME_APP_NAME;
import static com.studentpal.engine.Event.TAGNAME_APP_PKGNAME;
import static com.studentpal.engine.Event.TAGNAME_ERR_CODE;
import static com.studentpal.engine.Event.TAGNAME_ERR_DESC;
import static com.studentpal.engine.Event.TAGNAME_RESULT;
import static com.studentpal.engine.Event.TAGNAME_RULE_AUTH_TYPE;
import static com.studentpal.engine.Event.TAGNAME_RULE_REPEAT_ENDTIME;
import static com.studentpal.engine.Event.TAGNAME_RULE_REPEAT_STARTTIME;
import static com.studentpal.engine.Event.TAGNAME_RULE_REPEAT_TYPE;
import static com.studentpal.engine.Event.TAGNAME_RULE_REPEAT_VALUE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.studentpal.engine.ClientEngine;
import com.studentpal.engine.Event;
import com.studentpal.model.AccessCategory;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.model.exception.STDException;
import com.studentpal.model.rules.AccessRule;
import com.studentpal.model.rules.Recurrence;
import com.studentpal.model.rules.TimeRange;
import com.studentpal.util.logger.Logger;


public class SetAppAccessCategoryRequest extends Request {

  @Override
  public String getName() {
    return Event.TASKNAME_SetAppAccessCategory;
  }
  
  @Override
  public void execute() {
    try {
      JSONObject respObj = super.generateGenericReplyHeader(getName());
      JSONObject resultObj = new JSONObject();
      
      try {
        if (this.inputArguments == null) {
          respObj.put(TAGNAME_ERR_CODE, ERRCODE_MSG_FORMAT_ERR);
          //TODO add description to "result" obj
          
        } else {
          Map<Integer, AccessCategory> catesMap =
            new HashMap<Integer, AccessCategory>();
          
          JSONObject argsParam = new JSONObject(inputArguments);

          JSONArray catesAry = argsParam.getJSONArray(TAGNAME_ACCESS_CATEGORIES);
          retrieveAccessCategories(catesAry, catesMap);
            
          JSONArray appsAry = argsParam.getJSONArray(TAGNAME_APPLICATIONS);
          retrieveAppAccessCategory(appsAry, catesMap);

          List<AccessCategory> catesList = new ArrayList<AccessCategory>();
          for (Integer key : catesMap.keySet()) {
            catesList.add(catesMap.get(key));
          }

          //save to DB
          ClientEngine.getInstance().getDBaseManager().saveAccessCategoriesToDB(
              catesList);
          
          //update the access controller
          ClientEngine.getInstance().getAccessController().setAccessCategories(
              catesList);
          
          respObj.put(TAGNAME_ERR_CODE, ERRCODE_NOERROR);
        }
      } catch (STDException ex) {
        Logger.w(getName(), "In execute() got an error:" + ex.toString());
        respObj.put(TAGNAME_ERR_CODE, ERRCODE_MSG_FORMAT_ERR);
        resultObj.put(TAGNAME_ERR_DESC, ex.getMessage());
        
      } finally {
        if (resultObj.length() > 0) {
          respObj.put(TAGNAME_RESULT, resultObj);
        }
        if (respObj != null) {
          setOutputContent(respObj.toString());
        }
      }

    } catch (JSONException ex) {
      Logger.w(getName(), "In execute() got an error:" + ex.toString());
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////
  private void retrieveAppAccessCategory(
      JSONArray appsAry, Map<Integer, AccessCategory> sourceMap) throws STDException {
    if (appsAry == null) {
      throw new STDException(TAGNAME_APPLICATIONS+" is NULL in input arguments");
    }
    
    try {
      for (int i = 0; i < appsAry.length(); i++) {
        JSONObject appObj = appsAry.getJSONObject(i);
        
        String appName = appObj.getString(TAGNAME_APP_NAME);
        String pkgName = appObj.getString(TAGNAME_APP_PKGNAME);
        String className = appObj.getString(TAGNAME_APP_CLASSNAME);
        ClientAppInfo appInfo = new ClientAppInfo(appName, pkgName, className);
        
        int cateId = appObj.getInt(TAGNAME_ACCESS_CATE_ID);
        AccessCategory aCate = sourceMap.get(cateId);
        if (aCate != null) {
          aCate.addManagedApp(appInfo);
        }
      }// for apps

    } catch (JSONException ex) {
      Logger.w(getName(), "In execute() got an error:" + ex.toString());
      throw new STDException(ex.toString());
    }
  }
    
  private void retrieveAccessCategories(
      JSONArray catesAry, Map<Integer, AccessCategory> intoMap) throws STDException {
    if (catesAry == null) {
      throw new STDException(TAGNAME_ACCESS_CATEGORIES+" is NULL in input arguments");
    }
    
    try {
//      Map<Integer, AccessCategory> catesMap = new HashMap<Integer, AccessCategory>();
      
      for (int i=0; i<catesAry.length(); i++) {
        JSONObject cateObj = catesAry.getJSONObject(i);
  
        AccessCategory aCate = new AccessCategory();
        aCate.set_id(cateObj.getInt(TAGNAME_ACCESS_CATE_ID));
        aCate.set_name(cateObj.getString(TAGNAME_ACCESS_CATE_NAME));
  
        if (cateObj.has(TAGNAME_ACCESS_RULES) == false) continue;
        
        JSONArray rulesAry = cateObj.getJSONArray(TAGNAME_ACCESS_RULES);
        for (int m=0; m<rulesAry.length(); m++) {
          JSONObject ruleObj = rulesAry.getJSONObject(m);
          
          AccessRule aRule = new AccessRule();
          aRule.setAccessType(ruleObj.getInt(TAGNAME_RULE_AUTH_TYPE));
          Recurrence recur = Recurrence.getInstance(ruleObj.getInt(TAGNAME_RULE_REPEAT_TYPE));
          if (recur.getRecurType() != Recurrence.DAILY) {
            recur.setRecurValue(ruleObj.getInt(TAGNAME_RULE_REPEAT_VALUE));
          }
          aRule.setRecurrence(recur);
          
          JSONArray timerangeAry = ruleObj.getJSONArray(TAGNAME_ACCESS_TIMERANGES);
          for (int k=0; k<timerangeAry.length(); k++) {
            JSONObject trObj = timerangeAry.getJSONObject(k);
  
            TimeRange tr = new TimeRange();
            String time = trObj.getString(TAGNAME_RULE_REPEAT_STARTTIME);
            int idx = time.indexOf(':');
            int hour = Integer.parseInt(time.substring(0, idx));
            int min  = Integer.parseInt(time.substring(idx+1));
            tr.setStartTime(hour, min);
            
            time = trObj.getString(TAGNAME_RULE_REPEAT_ENDTIME);
            idx = time.indexOf(':');
            hour = Integer.parseInt(time.substring(0, idx));
            min  = Integer.parseInt(time.substring(idx+1));
            tr.setEndTime(hour, min);
            
            aRule.addTimeRange(tr);
          }//for time_ranges
          
          aCate.addAccessRule(aRule);
          
        }//for rules
        
        intoMap.put(aCate.get_id(), aCate);
        
      }//for cates

    } catch (JSONException ex) {
      Logger.w(getName(), "In execute() got an error:" + ex.toString());
      throw new STDException(ex.toString());
    }
  }

}