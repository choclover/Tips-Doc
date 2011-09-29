package studentpal.test.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;

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
  private String mDeviceId;
  
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
   * wsimport -d ./bin/ -s ./src -p studentpal.ws.wsclient http://coeustec.gicp.net:9090/StudentPal/PhoneConnector?wsdl
   */
  private PhoneConnectorWsService pcWsService;
  private PhoneConnectorWs pcWsInst;
  
  
  //////////////////////////////////////////////////////////////////////////////
  public WsClientCli() {
    // dcf = JaxWsDynamicClientFactory.newInstance();
    // wsClient = dcf.createClient(wsUrl);

    pcWsService = new PhoneConnectorWsService();
    pcWsInst = pcWsService.getPhoneConnectorWsPort();
    
    this.mDeviceId = new StringBuffer().append(phone_number1).append(
        phone_number2).append(phone_number3).toString();
  }
  
  public void wsSayHello() throws Exception {
    P(pcWsInst.sayHello("张三"));
  }
  
  public void wsGetAppList() {
    //next test client will use next phone number
    //phone_number3 = String.valueOf(Integer.parseInt(phone_number3)+1);
    
    P(pcWsInst.getAppList(this.mDeviceId));
  }

  public void wsSetAppAccessCategory() throws JSONException {
    JSONObject rootObj = new JSONObject();
    JSONArray catesObj = createAccessCategories();
    rootObj.put(Message.TAGNAME_ACCESS_CATEGORIES, catesObj);
    
    JSONArray appsObj = crateAppAccessCategory(
        ((JSONObject)catesObj.get(0)).getInt(Message.TAGNAME_ACCESS_CATE_ID));
    rootObj.put(Message.TAGNAME_APPLICATIONS, appsObj);
    
    String parmStr = rootObj.toString();
    P(pcWsInst.setAppAccessCategory(this.mDeviceId, parmStr));
  }
  
  public void wsGetPhoneStatus() {
    String phoneNo = new StringBuffer().append(phone_number1).append(
        phone_number2).append(phone_number3).toString();
    P(pcWsInst.getPhoneStatus(phoneNo));
  }
  
  //////////////////////////////////////////////////////////////////////////////
  private void parse_args(String[] args) {
    for (String arg : args) {
      if (arg.equals("-debug")) {
        bDebug = true;
      } else if (arg.equals("-phone")) {
        this.mDeviceId = "460003093130698";  //Defy / ME525
        
      } else if (arg.equals("-emulator")) {
        
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
  
  private JSONArray createAccessCategories() throws JSONException {
    JSONArray catesAry = new JSONArray();
    JSONArray rulesAry;
    JSONArray trsAry;
    
    JSONObject aCateObj;
    JSONObject aRuleObj;
    JSONObject aTrObj;
    
    /*
     * Set content for Access Cate 1
     */
    rulesAry = new JSONArray();
    
    //Rule 1
    trsAry = new JSONArray();
    aTrObj = new JSONObject();
    aTrObj.put(Message.TAGNAME_RULE_REPEAT_STARTTIME, "8:00");
    aTrObj.put(Message.TAGNAME_RULE_REPEAT_ENDTIME, "8:25");
    trsAry.put(aTrObj);
    
    aTrObj = new JSONObject();
    aTrObj.put(Message.TAGNAME_RULE_REPEAT_STARTTIME, "12:04");
    aTrObj.put(Message.TAGNAME_RULE_REPEAT_ENDTIME, "12:06");
    trsAry.put(aTrObj);
    
    aTrObj = new JSONObject();
    aTrObj.put(Message.TAGNAME_RULE_REPEAT_STARTTIME, "23:00");
    aTrObj.put(Message.TAGNAME_RULE_REPEAT_ENDTIME, "23:59");
    trsAry.put(aTrObj);
    
    aRuleObj = new JSONObject();
    aRuleObj.put(Message.TAGNAME_RULE_AUTH_TYPE, Message.ACCESS_TYPE_DENIED);
    aRuleObj.put(Message.TAGNAME_RULE_REPEAT_TYPE, Message.RECUR_TYPE_DAILY);
    //aRuleObj.put(Message.TAGNAME_RULE_REPEAT_VALUE, 0);
    aRuleObj.put(Message.TAGNAME_ACCESS_TIMERANGES, trsAry);
    
    rulesAry.put(aRuleObj);
    
    //Rule 2
    trsAry = new JSONArray();

    int hour = 8;
    int min = 30;
    aTrObj = new JSONObject();
    aTrObj.put(Message.TAGNAME_RULE_REPEAT_STARTTIME, ""+hour+":"+min);
    aTrObj.put(Message.TAGNAME_RULE_REPEAT_ENDTIME, ""+hour+":"+(min+1));
    trsAry.put(aTrObj);
    
    aTrObj = new JSONObject();
    aTrObj.put(Message.TAGNAME_RULE_REPEAT_STARTTIME, ""+hour+":"+(min+2));
    aTrObj.put(Message.TAGNAME_RULE_REPEAT_ENDTIME, ""+hour+":"+(min+9));
    trsAry.put(aTrObj);
    
    aRuleObj = new JSONObject();
    aRuleObj.put(Message.TAGNAME_RULE_AUTH_TYPE, Message.ACCESS_TYPE_PERMITTED);
    aRuleObj.put(Message.TAGNAME_RULE_REPEAT_TYPE, Message.RECUR_TYPE_WEEKLY);
    int recureVal = 0;
    recureVal |= (1 << (Calendar.TUESDAY-1) );
    recureVal |= (1 << (Calendar.WEDNESDAY-1) );
    recureVal |= (1 << (Calendar.THURSDAY-1) );
    aRuleObj.put(Message.TAGNAME_RULE_REPEAT_VALUE, recureVal);
    aRuleObj.put(Message.TAGNAME_ACCESS_TIMERANGES, trsAry);
    
    rulesAry.put(aRuleObj);
    
    aCateObj = new JSONObject();
    aCateObj.put(Message.TAGNAME_ACCESS_CATE_ID, 101);
    aCateObj.put(Message.TAGNAME_ACCESS_CATE_NAME, "Cate 1");
    aCateObj.put(Message.TAGNAME_ACCESS_RULES, rulesAry);
    
    catesAry.put(aCateObj);
    
    /*
     * Set content for Access Cate 2
     */
    //TODO
    aCateObj = new JSONObject();
    aCateObj.put(Message.TAGNAME_ACCESS_CATE_ID, 102);
    aCateObj.put(Message.TAGNAME_ACCESS_CATE_NAME, "Cate 2");
    aCateObj.put(Message.TAGNAME_ACCESS_RULES, (JSONArray)null);
    
    catesAry.put(aCateObj);
    
    return catesAry;
  }
  
  private JSONArray crateAppAccessCategory(int cateId) throws JSONException {
    JSONArray appsObj = new JSONArray();
    JSONObject anAppObj;
    
    anAppObj = new JSONObject();
    anAppObj.put(Message.TAGNAME_APP_NAME, "Messaging");
    anAppObj.put(Message.TAGNAME_APP_PKGNAME, "com.android.mms");
    anAppObj.put(Message.TAGNAME_APP_CLASSNAME, "com.android.mms.Messaging");
    anAppObj.put(Message.TAGNAME_ACCESS_CATE_ID, cateId);
    appsObj.put(anAppObj);

    anAppObj = new JSONObject();
    anAppObj.put(Message.TAGNAME_APP_NAME, "Alarmclock");
    anAppObj.put(Message.TAGNAME_APP_PKGNAME, "com.android.alarmclock");
    anAppObj.put(Message.TAGNAME_APP_CLASSNAME, "com.android.alarmclock.Alarmclock");
    anAppObj.put(Message.TAGNAME_ACCESS_CATE_ID, cateId);
    appsObj.put(anAppObj);
    
    anAppObj = new JSONObject();
    anAppObj.put(Message.TAGNAME_APP_NAME, "DeskClock");
    anAppObj.put(Message.TAGNAME_APP_PKGNAME, "com.android.deskclock");
    anAppObj.put(Message.TAGNAME_APP_CLASSNAME, "com.android.deskclock.DeskClock");
    anAppObj.put(Message.TAGNAME_ACCESS_CATE_ID, cateId);
    appsObj.put(anAppObj);

    anAppObj = new JSONObject();
    anAppObj.put(Message.TAGNAME_APP_NAME, "Browser");
    anAppObj.put(Message.TAGNAME_APP_PKGNAME, "com.android.browser");
    anAppObj.put(Message.TAGNAME_APP_CLASSNAME, "com.android.browser.Browser");
    anAppObj.put(Message.TAGNAME_ACCESS_CATE_ID, cateId);
    appsObj.put(anAppObj);
    
    anAppObj = new JSONObject();
    anAppObj.put(Message.TAGNAME_APP_NAME, "开心网");
    anAppObj.put(Message.TAGNAME_APP_PKGNAME, "com.kaixin001.activity");
//    anAppObj.put(Message.TAGNAME_APP_CLASSNAME, "com.kaixin001.activity");
    anAppObj.put(Message.TAGNAME_ACCESS_CATE_ID, cateId);
    appsObj.put(anAppObj);   
    
    anAppObj = new JSONObject();
    anAppObj.put(Message.TAGNAME_APP_NAME, "植物大战僵尸");
    anAppObj.put(Message.TAGNAME_APP_PKGNAME, "com.popcap.pvz");
//    anAppObj.put(Message.TAGNAME_APP_CLASSNAME, "com.kaixin001.activity");
    anAppObj.put(Message.TAGNAME_ACCESS_CATE_ID, cateId);
    appsObj.put(anAppObj);     
    
    return appsObj;
  }
  
  ///////////////////////////////////////////////////
  public static void main(String[] args) {
    System.out.println("enter WsClientCli's main()!");
    
    WsClientCli client = new WsClientCli();
    client.parse_args(args);
    client.execute();
  }

}