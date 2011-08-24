
package studentpal.ws;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.json.JSONException;
import org.json.JSONObject;

import studentpal.model.connection.ConnectionManager;
import studentpal.model.connection.PhoneConnection;
import studentpal.model.message.Message;
import studentpal.model.task.TaskDefinition;
import studentpal.model.task.TaskFactory;

@WebService(targetNamespace = "urn:cn.com.studentpal:PhoneConnectorWs")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class PhoneConnectorWs {
  
  @WebMethod
  public String sayHello(String name) {
    return "Hello: " + name;
  }
  
  @WebMethod
  public String getAppList(String mobileNo) {
    String replyStr = "";
    try {
      PhoneConnection pconn = ConnectionManager.retrieveConnection(mobileNo);
      if (pconn != null) {
        TaskDefinition task = TaskFactory.getInstance().createTask(Message.TASKNAME_GetAppList);
        pconn.executeTaskDef(task);
        
        replyStr = task.getReplyStr();
        pconn.finishTaskDef(task);
      
      } else {
        //TODO
      }
      
    } catch (JSONException e) {
      replyStr = generateParamsError();
    }
    
    return replyStr;
  }
  
  @WebMethod
  public String setAppAccessCategory(String mobileNo, String param) {
    String replyStr = "";
    try {
      TaskDefinition task = TaskFactory.getInstance().createTask(
          Message.TASKNAME_SetAppAccessCategory, param);

      PhoneConnection pconn = ConnectionManager.retrieveConnection(mobileNo);
      if (pconn != null) {
        pconn.executeTaskDef(task);  //will wait for response back

        replyStr = task.getReplyStr();
        pconn.finishTaskDef(task);

      } else {
        //TODO
      }
    } catch (JSONException e) {
      replyStr = generateParamsError();
    }
    return replyStr;
  }
  
  @WebMethod
  public String getPhoneStatus(String mobileNo) {
    //TODO
    String replyStr = "";
    return replyStr;
  }
  
  //////////////////////////////////////////////////////////////////////////////
  private String generateParamsError() {
    String result = "";
    
    try {
      JSONObject error = new JSONObject();
      error.put(Message.TAGNAME_MSG_TYPE, Message.MESSAGE_HEADER_ACK);
      error.put(Message.TAGNAME_ERR_CODE, Message.ERRCODE_FORMAT_ERR);
      result = error.toString();
      
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
    return result;
  }
}
