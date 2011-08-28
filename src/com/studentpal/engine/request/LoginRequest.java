package com.studentpal.engine.request;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.studentpal.engine.ClientEngine;
import com.studentpal.engine.Event;
import com.studentpal.model.ClientAppInfo;
import com.studentpal.util.logger.Logger;


public class LoginRequest extends Request {
  private String phoneNum = null;
  
  public LoginRequest(String phoneNum) {
    this.phoneNum  = phoneNum;
  }
  
  public String getName() {
    return Event.TASKNAME_LOGIN;
  }
  
  public void execute() {
    try {
      JSONObject argsObj = new JSONObject();
      argsObj.put(Event.TAGNAME_PHONE_NUM, this.phoneNum);
      
      JSONObject reqObj = new JSONObject();
      reqObj.put(Event.TAGNAME_MSG_TYPE, Event.MESSAGE_HEADER_REQ);
      reqObj.put(Event.TAGNAME_CMD_TYPE, getName());
      reqObj.put(Event.TAGNAME_ARGUMENTS, argsObj);
      
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