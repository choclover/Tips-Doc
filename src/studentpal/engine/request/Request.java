package studentpal.engine.request;

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
  
  public boolean isIncomingReq() {
    return bIncoming;
  }
  
  public boolean isOutgoingReq() {
    return !bIncoming;
  }
  
  public boolean isReplyReady() {
    return bReplyReady;
  }
  
  public String getName() {
    return Message.TASKNAME_Generic;
  }
  
  public JSONObject generateReplyHeader(String reqType) {
    JSONObject header = new JSONObject();
    
    return header;
  }
  
  public void setRequestSeq(int seq) {
    this.req_seq = seq;
  }
}