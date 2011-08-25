package studentpal.engine.request;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import studentpal.engine.ClientEngine;
import studentpal.engine.Message;
import studentpal.model.ClientAppInfo;
import studentpal.util.logger.Logger;

public class GetAppListRequest extends Request {

  public String getName() {
    return Message.TASKNAME_GetAppList;
  }
  
  public void execute() {
    List<ClientAppInfo> appList = ClientEngine.getInstance().getAppList();
    try {
      JSONObject respObj = super.generateGenericReplyHeader(getName());
      respObj.put(Message.TAGNAME_ERR_CODE, Message.ERRCODE_NOERROR);

      JSONArray appAry = new JSONArray();
      if (appList != null && appList.size() > 0) {
        for (ClientAppInfo appInfo : appList) {
          JSONObject app = new JSONObject();
          app.put(Message.TAGNAME_APP_NAME, appInfo.getAppName());
          app.put(Message.TAGNAME_APP_CLASSNAME, appInfo.getAppClassname());
          app.put(Message.TAGNAME_APP_ACCESS_TYPE, 1);
        }
      }

      JSONObject resultObj = new JSONObject();
      resultObj.put(Message.TAGNAME_APPLICATIONS, appAry);
      respObj.put(Message.TAGNAME_RESULT, resultObj);

      this.bIncoming = false;
      setOutputContent(respObj.toString());

    } catch (JSONException ex) {
      Logger.w(getName(), "In execute() got an error:" + ex.toString());
    }
  }

}