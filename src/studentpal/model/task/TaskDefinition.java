package studentpal.model.task;

import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import studentpal.model.connection.PhoneConnection;
import studentpal.model.message.Message;

public abstract class TaskDefinition {
  final static Logger logger = LoggerFactory.getLogger(TaskDefinition.class);
  
  public final static int TIMEOUT_SECONDS = 10*1000;  //seconds
  
  /* Fields */
  String taskName = Message.TASKNAME_Generic;

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
  
  public void populateRequestStr(JSONObject paramObj) throws JSONException {
    paramObj.putOnce(Message.TAGNAME_MSG_TYPE, Message.MESSAGE_HEADER_REQ);
    paramObj.putOnce(Message.TAGNAME_MSG_ID, this.getTaskId());
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
  
  public void handleReply(JSONObject response) {
    try {
      int errCode = response.getInt(Message.TAGNAME_ERR_CODE);
      JSONObject resultObj = response.getJSONObject(Message.TAGNAME_RESULT);
      if (errCode == Message.ERRCODE_NOERROR) {
        resultObj.put(Message.TAGNAME_RESULT, "SUCCESS");
      } else {
        resultObj.put(Message.TAGNAME_RESULT, "FAIL");
      }
      
      setReplyStr(resultObj.toString());
      
    } catch (JSONException e) {
      logger.warn(e.getLocalizedMessage());
    }

    notifyReplyWaiter();
  }
  
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
//      reply.put(Message.TAGNAME_MSG_TYPE, Message.MESSAGE_HEADER_ACK);
//      reply.put(Message.TAGNAME_ERR_CODE, Message.ERRCODE_TIMEOUT);
      reply.put(Message.TAGNAME_RESULT, "TIMEOUT");
      setReplyStr(reply.toString());
    } catch (JSONException e) {
      logger.warn(e.getLocalizedMessage());
    }
    
    notifyReplyWaiter();
  }
  
}

/*  Subclasses definition  */
class GetAppListTask extends TaskDefinition {
  public GetAppListTask() {
    taskName = Message.TASKNAME_GetAppList;
  }
  
  public void populateRequestStr(String args) throws JSONException {
    JSONObject paramObj = new JSONObject();
    
    super.populateRequestStr(paramObj);
    paramObj.putOnce(Message.TAGNAME_CMD_TYPE, taskName);
    setRequestStr(paramObj.toString());
  }
}

class SetAppAccessCategoryTask extends TaskDefinition {
  public SetAppAccessCategoryTask() {
    taskName = Message.TASKNAME_SetAppAccessCategory;
  }
  
  public void populateRequestStr(String args) throws JSONException {
    JSONObject paramObj = new JSONObject();
    
    super.populateRequestStr(paramObj);
    paramObj.putOnce(Message.TAGNAME_CMD_TYPE, taskName);
    if (args!=null && !args.isEmpty()) {
      paramObj.put(Message.TAGNAME_ARGUMENTS, args);
    }
    setRequestStr(paramObj.toString());
  }
}