package studentpal.model.task;

import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import studentpal.model.connection.PhoneConnection;
import static studentpal.model.message.Message.*;

public abstract class TaskDefinition {
  final static Logger logger = LoggerFactory.getLogger(TaskDefinition.class);
  
  public final static int TIMEOUT_SECONDS = 10*1000;  //seconds
  
  /* Fields */
  String taskName = TASKNAME_Generic;

  protected int taskId = 0;
  //the PhoneConnection to which the task is bound
  //protected PhoneConnection boundConn = null;
  
  protected String taskParams;
  protected String requestStr;
  protected String replyStr;
  
  protected Object waitLock = new Object();
  protected TimerTask timeoutTask;
  
  public int getTaskId() {
    return taskId;
  }
  public void setTaskId(int taskId) {
    this.taskId = taskId;
  }

  public String getTaskName() {
    return taskName;
  }
  
  public void setConnection(PhoneConnection conn) {
//    boundConn = conn;
  }
  
  public void setTaskParams(String params) {
    taskParams = params;
  }
  public String getTaskParams() {
    return taskParams;
  }
  
  public TimerTask getTimeoutTask() {
    if (timeoutTask == null) {
      timeoutTask = new TimerTask() {
        @Override
        public void run() {
          handleResponseTimeout();
        }
      };
    }
    return this.timeoutTask;
  }
  
  public void waitForReply() {
    synchronized(waitLock) {
      try {
        waitLock.wait();
      } catch (InterruptedException e) {
        logger.warn(e.getLocalizedMessage());
      }
    }
  }
  
/*  
  public void execute() throws IOException {
    try {
      if (boundConn == null) {
        throw new IOException("Bound PhoneConnection is NULL!");
      }
      if (requestStr==null || requestStr.isEmpty()) {
        throw new IOException("Request is NULL or empty!");
      }

      if (timeoutTask == null) {
        timeoutTask = new TimerTask() {
          @Override
          public void run() {
            handleResponseTimeout();
          }
        };
      }
      
      boundConn.addScheduledTask(timeoutTask, TaskFactory.TIMEOUT_SECONDS);
      boundConn.sendMessage(requestStr);
      
      synchronized(waitLock) {
        waitLock.wait();
      }

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
*/
  
  public void populateRequestHeader(JSONObject paramObj) throws JSONException {
    paramObj.putOnce(TAGNAME_MSG_TYPE, MESSAGE_HEADER_REQ);
    paramObj.putOnce(TAGNAME_MSG_ID, this.getTaskId());
  }
  
  public abstract void populateRequestStr(String args) throws JSONException;
  
  public String getRequestStr() throws JSONException {
    populateRequestStr(this.taskParams);
    return requestStr;
  }

  public void setRequestStr(String requestStr) {
    this.requestStr = requestStr;
  }

  public String getReplyStr() {
    return replyStr;
  }
  
  private void setReplyStr(String reply) {
    this.replyStr = reply;
  }
  
  public void handleReply(JSONObject raw_response) {
    if (raw_response == null) return;
    
    try {
      JSONObject replyObj = new JSONObject();
      int errCode = raw_response.getInt(TAGNAME_ERR_CODE);
      if (errCode == ERRCODE_NOERROR) {
        replyObj.put(TAGNAME_RESULT, "SUCCESS");
      } else {
        replyObj.put(TAGNAME_RESULT, "FAIL");
        replyObj.put(TAGNAME_ERR_CODE, raw_response.getInt(TAGNAME_ERR_CODE));
      }
      
      //populate the response body for each type of task
      populateResponseBody(raw_response, replyObj);
      
      setReplyStr(replyObj.toString());
      
    } catch (JSONException e) {
      logger.warn(e.getLocalizedMessage());
    }

    notifyReplyWaiter();
  }
  
  protected abstract void populateResponseBody(JSONObject raw_response, JSONObject replyObj)
    throws JSONException;
  
  protected void notifyReplyWaiter() {
    if (timeoutTask != null)
      timeoutTask.cancel();
    
    synchronized(waitLock) {
      waitLock.notify();
    }
  }
  
  public void finish() {
    //TODO: clean up resource in this task
  }
  
  private void handleResponseTimeout() {
    logger.warn("Task(id:"+taskId+") of type " +taskName+ " has timeout!");
    try {
      JSONObject reply = new JSONObject();
//      reply.put(TAGNAME_MSG_TYPE, MESSAGE_HEADER_ACK);
//      reply.put(TAGNAME_ERR_CODE, ERRCODE_TIMEOUT);
      reply.put(TAGNAME_RESULT, "TIMEOUT");
      setReplyStr(reply.toString());
    } catch (JSONException e) {
      logger.warn(e.getLocalizedMessage());
    }
    
    notifyReplyWaiter();
  }
  
}

/*  Inner-classes definition  */
class GetAppListTask extends TaskDefinition {
  public GetAppListTask() {
    taskName = TASKNAME_GetAppList;
  }
  
  public void populateRequestStr(String args) throws JSONException {
    JSONObject reqObj = new JSONObject();
    
    super.populateRequestHeader(reqObj);
    reqObj.putOnce(TAGNAME_CMD_TYPE, taskName);
    //No need to append arguments
    
    setRequestStr(reqObj.toString());
  }
  
  @Override
  protected void populateResponseBody(JSONObject raw_response, JSONObject replyObj) throws JSONException
  {
    JSONObject resultObj = raw_response.getJSONObject(TAGNAME_RESULT);
    if (resultObj != null) {
      JSONArray appsAry = resultObj.getJSONArray(TAGNAME_APPLICATIONS);
      if (appsAry != null) {
        replyObj.put(TAGNAME_APPLICATIONS, appsAry);  
      }
    }
  }
}

class SetAppAccessCategoryTask extends TaskDefinition {
  public SetAppAccessCategoryTask() {
    taskName = TASKNAME_SetAppAccessCategory;
  }
  
  public void populateRequestStr(String args) throws JSONException {
    JSONObject reqObj = new JSONObject();
    
    super.populateRequestHeader(reqObj);
    reqObj.putOnce(TAGNAME_CMD_TYPE, taskName);
    if (args!=null /*&& !args.isEmpty()*/) {
      reqObj.put(TAGNAME_ARGUMENTS, args);
    }
    
    setRequestStr(reqObj.toString());
  }

  @Override
  protected void populateResponseBody(JSONObject raw_response, JSONObject replyObj) 
    throws JSONException {
    //Nothing to add into response body
  }
}