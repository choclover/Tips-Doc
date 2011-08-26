package com.studentpal.engine.request;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.studentpal.engine.ClientEngine;
import com.studentpal.engine.Message;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.util.logger.Logger;


public class LoginRequest extends Request {
  private String phoneNum = null;
  
  public LoginRequest(String phoneNum) {
    this.phoneNum  = phoneNum;
  }
  
  public String getName() {
    return Message.TASKNAME_LOGIN;
  }
  
  public void execute() {
    try {
      JSONObject argsObj = new JSONObject();
      argsObj.put(Message.TAGNAME_PHONE_NUM, this.phoneNum);
      
      JSONObject reqObj = new JSONObject();
      reqObj.put(Message.TAGNAME_MSG_TYPE, Message.MESSAGE_HEADER_REQ);
      reqObj.put(Message.TAGNAME_CMD_TYPE, getName());
      reqObj.put(Message.TAGNAME_ARGUMENTS, argsObj);
      
      this.bIncoming = false;
      setOutputContent(reqObj.toString());

    } catch (JSONException ex) {
      Logger.w(getName(), "In execute() got an error:" + ex.toString());
    }
  }
  
  public boolean isOutputContentReady() {
    return true;
  }

}