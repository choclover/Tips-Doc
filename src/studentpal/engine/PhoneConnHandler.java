package studentpal.engine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Date;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import studentpal.model.connection.ConnectionManager;
import studentpal.model.connection.PhoneConnection;
import studentpal.model.message.Message;
import studentpal.model.task.TaskDefinition;
import studentpal.model.task.TaskFactory;

public class PhoneConnHandler extends IoHandlerAdapter {
  private final Logger logger = LoggerFactory.getLogger(PhoneConnHandler.class);
  
  @Override
  public void sessionCreated(IoSession session) {
    InetSocketAddress addr = (InetSocketAddress) session.getRemoteAddress();
    logger.info("Session CREATED from: "+addr.toString());
  }
  
  public void sessionOpened(IoSession session) {
    InetSocketAddress addr = (InetSocketAddress) session.getRemoteAddress();
    logger.info("Session OPENED from: "+addr.toString());
  }

  @Override
  public void messageReceived(IoSession session, Object message)
      throws Exception {
    if (message instanceof String) {
      handleMessage(session, (String)message);
    }
  }

  @Override
  public void exceptionCaught(IoSession session, Throwable cause)
      throws Exception {
//    super.exceptionCaught(session, cause);
    logger.warn(cause.toString());
//    logger.warn("Caught an exception!");
    
    if (cause instanceof IOException) {
      session.close(true);
    }
  }
  
  @Override
  public void sessionClosed(IoSession session) throws Exception {
    PhoneConnection pconn = (PhoneConnection)session.getAttribute(PhoneConnection.ATTR_TAGNAME);
    if (pconn != null) {
      ConnectionManager.removeConnection(pconn);
      pconn = null;
    }
  }
  
  private void handleMessage(IoSession session, String message) {
    try {
      JSONObject msgObj = new JSONObject(message);
      
      String msgType = msgObj.getString(Message.TAGNAME_MSG_TYPE);
      if (msgType.equals(Message.MESSAGE_HEADER_ACK)) {
        handleResponse(session, msgObj);
      } else if (msgType.equals(Message.MESSAGE_HEADER_REQ)) {
        handleRequest(session, msgObj);
      }
    } catch (JSONException ex) {
      logger.warn(ex.toString());
    }
  }
  
  private void handleResponse(IoSession session, JSONObject response) 
    throws JSONException {
//    int errcode = response.getInt(Message.TAGNAME_ERR_CODE); 
//    Object result = response.get(Message.TAGNAME_RESULT);
//    Message msg = EvntPool.get();
//    msg.setData(0, errcode, result);
//    EvntQueue.put(msg);
    
    /*
     * TODO: Will moved to MessageHandler.handleEvent()
     */
    PhoneConnection pconn = 
      (PhoneConnection)session.getAttribute(PhoneConnection.ATTR_TAGNAME);
    if (pconn != null) {
      int taskId = response.getInt(Message.TAGNAME_MSG_ID);
      TaskDefinition task = pconn.retrieveTaskDef(taskId);
      if (task != null) {
        task.handleReply(response);
      }
    }
  }
  
  private void handleRequest(IoSession session, JSONObject request) 
    throws JSONException {
    String cmdType = request.getString(Message.TAGNAME_CMD_TYPE);
    
    if (cmdType.equals(TaskDefinition.TASK_LOGIN)) {
      JSONObject argObj = request.getJSONObject(Message.TAGNAME_ARGUMENTS); 
      String phoneNo    = argObj.getString(Message.TAGNAME_PHONE_NUM);
      
      PhoneConnection pconn = new PhoneConnection(session, phoneNo);
      ConnectionManager.addConnection(phoneNo, pconn);
      
      session.setAttribute(PhoneConnection.ATTR_TAGNAME, pconn);
    
    } else if (cmdType.equals(TaskDefinition.TASK_LOGOUT)) {
      JSONObject argObj = request.getJSONObject(Message.TAGNAME_ARGUMENTS); 
      String phoneNo    = argObj.getString(Message.TAGNAME_PHONE_NUM);
      
      ConnectionManager.removeConnection(phoneNo);
    }
  }

}
