package com.studentpal.engine.request;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.studentpal.engine.ClientEngine;
import static com.studentpal.engine.Event.*;
import com.studentpal.model.AccessCategory;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.model.rules.AccessRule;
import com.studentpal.model.rules.Recurrence;
import com.studentpal.model.rules.TimeRange;
import com.studentpal.util.logger.Logger;


public class SetAccessCategoriesRequest extends Request {

  public String getName() {
    return TASKNAME_SetAccessCategories;
  }
  
  public void execute() {
    try {
      JSONObject respObj = super.generateGenericReplyHeader(getName());
      
      try {
        JSONObject resultObj = new JSONObject();
        
        if (this.inputContent == null) {
          respObj.put(TAGNAME_ERR_CODE, ERRCODE_FORMAT_ERR);
          //TODO add description to "result" obj
        } else {
          JSONObject rootObj = new JSONObject(inputContent);
          JSONObject argsObj = rootObj.getJSONObject(TAGNAME_ARGUMENTS);
          JSONArray catesAry = argsObj.getJSONArray(TAGNAME_ACCESS_CATEGORIES);
          
          if (catesAry != null) {
            List<AccessCategory> cateList = new ArrayList<AccessCategory>();
            
            for (int i=0; i<catesAry.length(); i++) {
              JSONObject cateObj = catesAry.getJSONObject(i);

              AccessCategory aCate = new AccessCategory();
              aCate.set_id(cateObj.getInt(TAGNAME_ACCESS_CATE_ID));
              aCate.set_name(cateObj.getString(TAGNAME_ACCESS_CATE_NAME)); 

              JSONArray rulesAry = cateObj.getJSONArray(TAGNAME_ACCESS_RULES);
              for (int m=0; m<rulesAry.length(); m++) {
                JSONObject ruleObj = rulesAry.getJSONObject(m);
                
                AccessRule aRule = new AccessRule();
                aRule.setAccessType(ruleObj.getInt(TAGNAME_RULE_AUTH_TYPE));
                Recurrence recur = Recurrence.getInstance(ruleObj.getInt(TAGNAME_RULE_REPEAT_TYPE));
                recur.setRecurValue(ruleObj.getInt(TAGNAME_RULE_REPEAT_VALUE));
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
                  
                  tr = new TimeRange();
                  time = trObj.getString(TAGNAME_RULE_REPEAT_ENDTIME);
                  idx = time.indexOf(':');
                  hour = Integer.parseInt(time.substring(0, idx));
                  min  = Integer.parseInt(time.substring(idx+1));
                  tr.setEndTime(hour, min);
                  
                  aRule.addTimeRange(tr);
                }//for time_ranges
                
                aCate.addAccessRule(aRule);
                
              }//for rules
              
              cateList.add(aCate);
              
            }//for cates
            
            ClientEngine.getInstance().getAccessController()
                .setAccessCategories(cateList);
            
          } else {
            respObj.put(TAGNAME_ERR_CODE, ERRCODE_FORMAT_ERR);
            resultObj.put(TAGNAME_ERR_DESC, TAGNAME_APPLICATIONS
                + " is NULL in input arguments");
          }
    
          respObj.put(TAGNAME_ERR_CODE, ERRCODE_NOERROR);
        
        }
        
        if (resultObj.length() > 0) {
          respObj.put(TAGNAME_RESULT, resultObj);
        }
        
      } catch (Exception ex) {
        Logger.w(getName(), "In execute() got an error:" + ex.toString());
        respObj.put(TAGNAME_ERR_CODE, ERRCODE_SERVER_INTERNAL_ERR);

      } finally {
        if (respObj != null) {
          setOutputContent(respObj.toString());
        }        
      }

    } catch (JSONException ex) {
      Logger.w(getName(), "In execute() got an error:" + ex.toString());
    }
  }

}