package studentpal.engine.request;

import org.json.JSONException;
import org.json.JSONObject;

import studentpal.app.MessageHandler;
import studentpal.engine.Message;

public abstract class Request /*extends Message*/ {
  /*
   * Constants
   */
  
  /*
   * Field Members
   */
  protected boolean bIncoming = true;
  protected boolean bReplyReady = false;
  protected String replyStr = null;
  protected int req_seq = Message.INVALID_MSG_ID;
  
  public void execute(MessageHandler msgHandler) {
  }
  
  public abstract void execute();
  
  public String getName() {
    return Message.TASKNAME_Generic;
  }
  
  public boolean isIncomingReq() {
    return bIncoming;
  }
  
  public boolean isOutgoingReq() {
    return !bIncoming;
  }
  
  public boolean isReplyReady() {
    return bReplyReady;
  }
  
  public String getReplyStr() {
    return replyStr;
  }
  
  public JSONObject generateGenericReplyHeader(String cmd_type) 
    throws JSONException {
    JSONObject header = new JSONObject();
    header.put(Message.TAGNAME_MSG_TYPE, Message.MESSAGE_HEADER_ACK);
    header.put(Message.TAGNAME_MSG_ID, req_seq);
    header.put(Message.TAGNAME_CMD_TYPE, cmd_type);
    
    return header;
  }
  
  public void setRequestSeq(int seq) {
    this.req_seq = seq;
  }
}