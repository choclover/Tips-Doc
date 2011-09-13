package com.studentpal.engine.request;

import org.json.JSONException;
import org.json.JSONObject;

import com.studentpal.app.MessageHandler;
import com.studentpal.engine.Event;


public abstract class Request /*extends Message*/ {
  /*
   * Constants
   */
  
  /*
   * Field Members
   */
  protected boolean bIncoming = true;
  protected boolean bOutputContentReady = false;
  
  protected String inputContent = null;
  protected String outputContent = null;

  protected int req_seq = Event.MSG_ID_INVALID;
  
  public void execute(MessageHandler msgHandler) {
  }
  
  public abstract void execute();
  
  public String getName() {
    return Event.TASKNAME_Generic;
  }
  
  public boolean isIncomingReq() {
    return bIncoming;
  }
  
  public boolean isOutgoingReq() {
    return !bIncoming;
  }
  
  public boolean isOutputContentReady() {
    return bOutputContentReady;
  }
  
  public String getOutputContent() {
    return outputContent;
  }
  
  public void setOutputContent(String content) {
    this.bIncoming = false;
    outputContent = content;
    if (content!=null && content.trim().length() > 0) {
      this.bOutputContentReady = true;
    } else {
      this.bOutputContentReady = false;
    }
  }
  
  public String getInputContent() {
    return inputContent;
  }
  
  public void setInputContent(String content) {
    inputContent = content;
  }
  
  public JSONObject generateGenericReplyHeader(String cmd_type) 
    throws JSONException {
    JSONObject header = new JSONObject();
    header.put(Event.TAGNAME_MSG_TYPE, Event.MESSAGE_HEADER_ACK);
    header.put(Event.TAGNAME_MSG_ID, req_seq);
    header.put(Event.TAGNAME_CMD_TYPE, cmd_type);
    
    return header;
  }
  
  public void setRequestSeq(int seq) {
    this.req_seq = seq;
  }
}