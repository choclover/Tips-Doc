package studentpal.test.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import studentpal.model.message.Message;
import studentpal.ws.WsService;
import studentpal.ws.wsclient.PhoneConnectorWs;
import studentpal.ws.wsclient.PhoneConnectorWsService;

//import org.apache.cxf.endpoint.Client;
//import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;

public class WsClientCli {
  final Logger logger = LoggerFactory.getLogger(WsClientCli.class);
  
  /*
   * Member fields
   */
  private boolean bDebug = false;
  private String wsUrl = WsService.getWsUrl();
  
  static String phone_number1 = "155";
  static String phone_number2 = "5521";
  static String phone_number3 = "5554";
  
  /*
   * CXF WS client
   */
//  private JaxWsDynamicClientFactory dcf;
//  private Client cxfWsClient;
  
  /*
   * JDK6 WS client
   * wsimport -d ./bin/ -s ./src -p studentpal.ws.wsclient http://localhost:9090/StudentPal/PhoneConnector?wsdl
   */
  private PhoneConnectorWsService pcWsService;
  private PhoneConnectorWs pcWsInst;
  
  
  //////////////////////////////////////////////////////////////////////////////
  public WsClientCli() {
    // dcf = JaxWsDynamicClientFactory.newInstance();
    // wsClient = dcf.createClient(wsUrl);

    pcWsService = new PhoneConnectorWsService();
    pcWsInst = pcWsService.getPhoneConnectorWsPort();
  }
  
  public void wsSayHello() throws Exception {
    P(pcWsInst.sayHello("张三"));  
  }
  
  public void wsGetAppList() {
    String phoneNo = new StringBuffer().append(phone_number1).append(
        phone_number2).append(phone_number3).toString();
    //next test client will use next phone number 
    //phone_number3 = String.valueOf(Integer.parseInt(phone_number3)+1);
    
    P(pcWsInst.getAppList(phoneNo));
  }

  public void wsSetAppAccessCategory() {

  }
  
  public void wsGetPhoneStatus() {
    String phoneNo = new StringBuffer().append(phone_number1).append(
        phone_number2).append(phone_number3).toString();
    P(pcWsInst.getPhoneStatus(phoneNo));
  }
  
  //////////////////////////////////////////////////////////////////////////////
  private void handleIncomingMsg(String reqStr) {
    JSONObject resp = new JSONObject();
    try {
      JSONObject req = new JSONObject(reqStr);

      String msg_type = req.getString(Message.TAGNAME_MSG_TYPE);
      if (msg_type.equals(Message.MESSAGE_HEADER_ACK)) {
        logger.info("This is a ACK message, will ignore it.");
        return;
      }

      // We will only handle requests.
      String cmd_type = req.getString(Message.TAGNAME_CMD_TYPE);
      int req_seq = req.getInt(Message.TAGNAME_MSG_ID);

      resp.put(Message.TAGNAME_MSG_TYPE, Message.MESSAGE_HEADER_ACK);
      resp.put(Message.TAGNAME_MSG_ID, req_seq);
      resp.put(Message.TAGNAME_ERR_CODE, Message.ERRCODE_NOERROR);
      resp.put(Message.TAGNAME_CMD_TYPE, cmd_type);

      JSONObject result = new JSONObject();
      // 澶勭悊GetAppList鍛戒护
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
        app.put(Message.TAGNAME_APP_CLASSNAME,
            "com.android.alarmclock.Alarmclock");
        app.put(Message.TAGNAME_ACCESS_CATE_ID, 2);
        applications.put(app);

        app = new JSONObject();
        app.put(Message.TAGNAME_APP_NAME, "Camera");
        app.put(Message.TAGNAME_APP_PKGNAME, "com.android.camera");
        app.put(Message.TAGNAME_APP_CLASSNAME, "com.android.camera.Camera");
        applications.put(app);

        result.put(Message.TAGNAME_APPLICATIONS, applications);

      } else if (cmd_type.equals(Message.TASKNAME_SetAppAccessCategory)) {
        // TODO
      }
      resp.put(Message.TAGNAME_RESULT, result);

      // String resultStr = resp.toString();
      // if (resultStr!=null && !resultStr.isEmpty()) {
      // System.out.println("Sending back response to server:\n\t"+resultStr);
      // sendMessage(resultStr);
      // }

    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void parse_args(String[] args) {
    for (String arg : args) {
      if (arg.equals("-debug")) {
        bDebug = true;
      }
    }
  }
  
  private void execute() {
    boolean stop = false;
    while (!stop) {
      try {      
        printWsUsage();
        
        String cmd = new BufferedReader(new InputStreamReader(System.in))
            .readLine();
        if (cmd.isEmpty()) continue;

        int idx = Integer.parseInt(cmd);
        switch (idx) {
        case 1:
          wsSayHello();
          break;
          
        case 2:
          wsGetAppList();
          break;

        case 3:
          wsSetAppAccessCategory();
          break;
          
        case 4:
          wsGetPhoneStatus();
          break;

        case 0:
          stop = true;
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
    P("\n");
    P("*******************************");
    P("1. SayHello");
    P("2. GetAppList");
    P("3. SetAppAccessCategory");
    P("4. GetPhoneStatus");
    
    P("0. Exit");
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