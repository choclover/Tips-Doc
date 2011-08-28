package com.studentpal.engine.request;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.studentpal.engine.ClientEngine;
import com.studentpal.engine.Event;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.util.logger.Logger;


public class GetAppListRequest extends Request {

  public String getName() {
    return Event.TASKNAME_GetAppList;
  }
  
  public void execute() {
    List<ClientAppInfo> appList = ClientEngine.getInstance().getAppList();
    try {
      JSONObject respObj = super.generateGenericReplyHeader(getName());
      respObj.put(Event.TAGNAME_ERR_CODE, Event.ERRCODE_NOERROR);

      JSONArray appAry = new JSONArray();
      if (appList != null && appList.size() > 0) {
        for (ClientAppInfo appInfo : appList) {
          JSONObject app = new JSONObject();
          app.put(Event.TAGNAME_APP_NAME, appInfo.getAppName());
          app.put(Event.TAGNAME_APP_CLASSNAME, appInfo.getAppClassname());
          app.put(Event.TAGNAME_APP_ACCESS_TYPE, 1);
          
          appAry.put(app);
        }
      }

      JSONObject resultObj = new JSONObject();
      resultObj.put(Event.TAGNAME_APPLICATIONS, appAry);
      respObj.put(Event.TAGNAME_RESULT, resultObj);

      this.bIncoming = false;
      setOutputContent(respObj.toString());

    } catch (JSONException ex) {
      Logger.w(getName(), "In execute() got an error:" + ex.toString());
    }
  }

}