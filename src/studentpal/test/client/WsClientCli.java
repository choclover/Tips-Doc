package studentpal.test.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import studentpal.engine.ServerEngine;
import studentpal.model.codec.AsCodecFactory;
import studentpal.model.message.Message;

//import org.apache.cxf.endpoint.Client;
//import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;

public class WsClientCli {
  final Logger logger = LoggerFactory.getLogger(WsClientCli.class);
  
  BufferedWriter bwriter;
  BufferedReader bdis;
  BufferedInputStream bis;
  BufferedOutputStream bos;

  static String phone_number1 = "155";
  static String phone_number2 = "5521";
  static String phone_number3 = "5554";

  boolean loggined = false;
  boolean bDebug = false;
  String phoneNo = "";
  
  public String createOutgoingMsg(String cmd) {
    String msg = null;
    
    try {
      JSONObject root = new JSONObject();
      root.put(Message.TAGNAME_MSG_TYPE, Message.MESSAGE_HEADER_REQ);
      
      if (cmd.equalsIgnoreCase("login")) {
        root.put(Message.TAGNAME_CMD_TYPE, Message.TASKNAME_LOGIN);
        root.put(Message.TAGNAME_MSG_ID, Message.MSG_ID_NOTUSED);
        
        JSONObject args = new JSONObject();
        this.phoneNo = new StringBuffer().append(phone_number1).append(
            phone_number2).append(phone_number3).toString();
        //next test client will use next phone number 
        phone_number3 = String.valueOf(Integer.valueOf(phone_number3).intValue()+1);
        args.put(Message.TAGNAME_PHONE_NUM, phoneNo);

        root.put(Message.TAGNAME_ARGUMENTS, args);
        loggined = true;
        
      } else if (cmd.equalsIgnoreCase("logout") && loggined) {
        root.put(Message.TAGNAME_CMD_TYPE, Message.TASKNAME_LOGOUT);
        root.put(Message.TAGNAME_MSG_ID, Message.MSG_ID_NOTUSED);
        
        JSONObject args = new JSONObject();
        args.put(Message.TAGNAME_PHONE_NUM, phoneNo);
        
        root.put(Message.TAGNAME_ARGUMENTS, args);
        loggined = false;
        
      } else {
        return null;
      }
      
      msg = root.toString();
      
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return msg;
  }

  private void handleIncomingMsg(String reqStr) {
    if (bDebug) {
      try {
        int sleep = 40;
        System.out.println("I am ready to sleep "+sleep+ " seconds...");
        Thread.sleep(sleep * 1000);
        System.out.println("Sleep over!");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return;
    }
    
    JSONObject resp = new JSONObject();
    try {
      JSONObject req = new JSONObject(reqStr);
      
      String msg_type = req.getString(Message.TAGNAME_MSG_TYPE);
      if (msg_type.equals(Message.MESSAGE_HEADER_ACK)) {
        logger.info("This is a ACK message, will ignore it.");
        return;
      }
      
      //We will only handle requests.
      String cmd_type = req.getString(Message.TAGNAME_CMD_TYPE);
      int req_seq = req.getInt(Message.TAGNAME_MSG_ID);

      resp.put(Message.TAGNAME_MSG_TYPE, Message.MESSAGE_HEADER_ACK);
      resp.put(Message.TAGNAME_MSG_ID, req_seq);
      resp.put(Message.TAGNAME_ERR_CODE, Message.ERRCODE_NOERROR);
      resp.put(Message.TAGNAME_CMD_TYPE, cmd_type);
      
      JSONObject result = new JSONObject();
      //处理GetAppList命令
      if (cmd_type.equals(Message.TASKNAME_GetAppList)) {
        JSONArray applications = new JSONArray();
        
        JSONObject app = new JSONObject();
        app.put(Message.TAGNAME_APP_NAME, "Browser");
        app.put(Message.TAGNAME_APP_PKGNAME, "com.android.browser");
        app.put(Message.TAGNAME_APP_CLASSNAME, "com.android.browser.Browser");
        app.put(Message.TAGNAME_ACCESS_CATE_ID, 1);
        applications.put(app);

        app = new JSONObject();
        app.put(Message.TAGNAME_APP_NAME, "Messaging");
        app.put(Message.TAGNAME_APP_PKGNAME, "com.android.mms");
        app.put(Message.TAGNAME_APP_CLASSNAME, "com.android.mms.Messaging");
        app.put(Message.TAGNAME_ACCESS_CATE_ID, 2);
        applications.put(app);
    
        app = new JSONObject();
        app.put(Message.TAGNAME_APP_NAME, "Alarm Clock");
        app.put(Message.TAGNAME_APP_PKGNAME, "com.android.alarmclock");
        app.put(Message.TAGNAME_APP_CLASSNAME, "com.android.alarmclock.Alarmclock");
        app.put(Message.TAGNAME_ACCESS_CATE_ID, 2);
        applications.put(app);
        
        app = new JSONObject();
        app.put(Message.TAGNAME_APP_NAME, "Camera");
        app.put(Message.TAGNAME_APP_PKGNAME, "com.android.camera");
        app.put(Message.TAGNAME_APP_CLASSNAME, "com.android.camera.Camera");
        applications.put(app);
        
        result.put(Message.TAGNAME_APPLICATIONS, applications);
        
      } else if (cmd_type.equals(Message.TASKNAME_SetAppAccessCategory)) {
        //TODO
      }
      resp.put(Message.TAGNAME_RESULT, result);
      
//      String resultStr = resp.toString();
//      if (resultStr!=null && !resultStr.isEmpty()) {
//        System.out.println("Sending back response to server:\n\t"+resultStr);
//        sendMessage(resultStr);
//      }
      
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////
  public void wsGetAppList() {

  }

  public void wsSetAppAccessCategory() {

  }
  
  public void wsGetPhoneStatus() {

  }

  private void parse_args(String[] args) {
    for (String arg : args) {
      if (arg.equals("-debug")) {
        bDebug = true;
      }
    }
  }
  
  private void execute() {
    while (true) {
      try {      
        printWsUsage();
        
        String cmd = new BufferedReader(new InputStreamReader(System.in))
            .readLine();
        if (cmd.isEmpty()) continue;

        int idx = Integer.parseInt(cmd);
        switch (idx) {
        case 1:
          wsGetAppList();
          break;

        case 2:
          wsSetAppAccessCategory();
          break;
          
        case 3:
          wsGetPhoneStatus();
          break;

        default:
          break;
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    
  }
  private static void printWsUsage() {
    P("\n\n");
    P("*******************************");
    P("1. GetAppList");
    P("2. SetAppAccessCategory");
    P("3. GetPhoneStatus");
    P("*******************************");
    P("Choose a command: ");
  }
  
  private static void P(String s) {
    System.out.println(s); 
  }
  
  ///////////////////////////////////////////////////
  public static void main(String[] args) {
    WsClientCli client = new WsClientCli();
    client.parse_args(args);
    client.execute();
  }

}