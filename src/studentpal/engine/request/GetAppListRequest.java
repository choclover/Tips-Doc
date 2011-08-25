package studentpal.engine.request;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import studentpal.engine.ClientEngine;
import studentpal.engine.Message;
import studentpal.model.ClientAppInfo;

public class GetAppListRequest extends Request {

  public String getName() {
    return Message.TASKNAME_GetAppList;
  }
  
  public void execute() {
    List<ClientAppInfo> appList = ClientEngine.getInstance().getAppList();
    if (appList != null) {
      try {

        JSONObject respObj = super.
        
        resp.put(Message.TAGNAME_MSG_TYPE, Message.MESSAGE_HEADER_ACK);
        resp.put(Message.TAGNAME_MSG_ID, req_seq);
        resp.put(Message.TAGNAME_ERR_CODE, Message.ERRCODE_NOERROR);
        resp.put(Message.TAGNAME_CMD_TYPE, cmd_type);
        
        JSONObject result = new JSONObject();
        if (cmd_type.equals(Message.TASKNAME_GetAppList)) {
          JSONArray applications = new JSONArray();
          
          JSONObject app = new JSONObject();
          app.put(Message.TAGNAME_APP_NAME, "Browser");
          app.put(Message.TAGNAME_APP_CLASSNAME, "com.android.browser");
          app.put(Message.TAGNAME_APP_ACCESS_TYPE, 1);
          applications.put(app);
          
          app = new JSONObject();
          app.put(Message.TAGNAME_APP_NAME, "Alarm Clock");
          app.put(Message.TAGNAME_APP_CLASSNAME, "com.android.alarmclock");
          app.put(Message.TAGNAME_APP_ACCESS_TYPE, 2);
          applications.put(app);
          
          app = new JSONObject();
          app.put(Message.TAGNAME_APP_NAME, "Camera");
          app.put(Message.TAGNAME_APP_CLASSNAME, "com.android.camera");
          applications.put(app);
          
          result.put(Message.TAGNAME_APPLICATIONS, applications);

        }
        resp.put(Message.TAGNAME_RESULT, result);
        
        
        
        
        
        this.replyStr = "";
        this.bReplyReady = true;
        
      } catch (JSONException ex) {
        
      
      }
    

    }
  }
  

}