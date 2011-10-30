package studentpal.test.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static studentpal.model.message.Message.*;
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
  private File cateDefFile = null;
  
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
   * wsimport -d ./bin/ -s ./src -p studentpal.ws.wsclient http://192.168.1.250:9090/StudentPal/PhoneConnector?wsdl
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
    JSONArray catesObj = null;
    JSONArray appsObj = null;
    
    if (this.cateDefFile != null) {
      catesObj = new JSONArray();
      appsObj = new JSONArray();
      loadCateDefFromFile(catesObj, appsObj);
    } else {
      catesObj = createAccessCategories();
      appsObj = crateAppAccessCategory(
        ((JSONObject)catesObj.get(0)).getInt(TAGNAME_ACCESS_CATE_ID));
    }
    
    rootObj.put(TAGNAME_ACCESS_CATEGORIES, catesObj);
    rootObj.put(TAGNAME_APPLICATIONS, appsObj);
    
    String parmStr = rootObj.toString();
    logger.info(parmStr);
    P(pcWsInst.setAppAccessCategory(this.mDeviceId, parmStr));
  }
  
  public void wsGetPhoneStatus() {
    String phoneNo = new StringBuffer().append(phone_number1).append(
        phone_number2).append(phone_number3).toString();
    P(pcWsInst.getPhoneStatus(phoneNo));
  }
  
  //////////////////////////////////////////////////////////////////////////////
  private void parse_args(String[] args) {
    for (int i=0; i<args.length; i++) {
      String arg = args[i];
      if (arg.equals("-debug")) {
        bDebug = true;
      } else if (arg.equals("-phone")) {
        this.mDeviceId = "460003093130698";  //IMSI of Defy(ME525)
        logger.info("\n** Running in PHONE mode, connecting to IMSI "+mDeviceId+" **");
        
      } else if (arg.equals("-emulator")) {
        
      } else if (arg.equals("-catexml")) {
        if (i+1 < args.length) {
          String fname = args[i+1];
          //logger.info("Cate XML: "+fname);
          File file = new File(fname);
          if (file.exists()) {
            cateDefFile = file;
            logger.info("\n** Loading Category source XML "+fname+" **");
          } else {
            cateDefFile = null;
            logger.warn("Category source XML "+fname+" NOT exists!");
          }
        }
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
    aTrObj.put(TAGNAME_RULE_REPEAT_STARTTIME, "8:00");
    aTrObj.put(TAGNAME_RULE_REPEAT_ENDTIME, "8:25");
    trsAry.put(aTrObj);
    
    aTrObj = new JSONObject();
    aTrObj.put(TAGNAME_RULE_REPEAT_STARTTIME, "16:05");
    aTrObj.put(TAGNAME_RULE_REPEAT_ENDTIME, "16:06");
    trsAry.put(aTrObj);
    
    aTrObj = new JSONObject();
    aTrObj.put(TAGNAME_RULE_REPEAT_STARTTIME, "23:00");
    aTrObj.put(TAGNAME_RULE_REPEAT_ENDTIME, "23:59");
    trsAry.put(aTrObj);
    
    aRuleObj = new JSONObject();
    aRuleObj.put(TAGNAME_RULE_AUTH_TYPE, ACCESS_TYPE_DENIED);
    aRuleObj.put(TAGNAME_RULE_REPEAT_TYPE, RECUR_TYPE_DAILY);
    //aRuleObj.put(TAGNAME_RULE_REPEAT_VALUE, 0);
    aRuleObj.put(TAGNAME_ACCESS_TIMERANGES, trsAry);
    
    rulesAry.put(aRuleObj);
    
    //Rule 2
    trsAry = new JSONArray();

    int hour = 19;
    int min = 32;
    aTrObj = new JSONObject();
    aTrObj.put(TAGNAME_RULE_REPEAT_STARTTIME, ""+hour+":"+min);
    aTrObj.put(TAGNAME_RULE_REPEAT_ENDTIME, ""+hour+":"+(min+1));
    trsAry.put(aTrObj);
    
    aTrObj = new JSONObject();
    aTrObj.put(TAGNAME_RULE_REPEAT_STARTTIME, ""+hour+":"+(min+2));
    aTrObj.put(TAGNAME_RULE_REPEAT_ENDTIME, ""+hour+":"+(min+3));
    trsAry.put(aTrObj);
    
    aRuleObj = new JSONObject();
    aRuleObj.put(TAGNAME_RULE_AUTH_TYPE, ACCESS_TYPE_PERMITTED);
    aRuleObj.put(TAGNAME_RULE_REPEAT_TYPE, RECUR_TYPE_WEEKLY);
    int recureVal = 0;
    recureVal |= (1 << (Calendar.TUESDAY-1) );
    recureVal |= (1 << (Calendar.WEDNESDAY-1) );
    recureVal |= (1 << (Calendar.THURSDAY-1) );
    recureVal |= (1 << (Calendar.FRIDAY-1) );
    aRuleObj.put(TAGNAME_RULE_REPEAT_VALUE, recureVal);
    aRuleObj.put(TAGNAME_ACCESS_TIMERANGES, trsAry);
    
    //rulesAry.put(aRuleObj);
    
    //------------------------------------
    aCateObj = new JSONObject();
    aCateObj.put(TAGNAME_ACCESS_CATE_ID, 101);
    aCateObj.put(TAGNAME_ACCESS_CATE_NAME, "Cate 1");
    aCateObj.put(TAGNAME_ACCESS_RULES, rulesAry);
    
    catesAry.put(aCateObj);
    
    /*
     * Set content for Access Cate 2
     */
    //TODO
    aCateObj = new JSONObject();
    aCateObj.put(TAGNAME_ACCESS_CATE_ID, 102);
    aCateObj.put(TAGNAME_ACCESS_CATE_NAME, "Cate 2");
    aCateObj.put(TAGNAME_ACCESS_RULES, (JSONArray)null);
    
    catesAry.put(aCateObj);
    
    return catesAry;
  }
  
  private JSONArray crateAppAccessCategory(int cateId) throws JSONException {
    JSONArray appsObj = new JSONArray();
    JSONObject anAppObj;
    
    anAppObj = new JSONObject();
    anAppObj.put(TAGNAME_APP_NAME, "Messaging");
    anAppObj.put(TAGNAME_APP_PKGNAME, "com.android.mms");
    anAppObj.put(TAGNAME_APP_CLASSNAME, "com.android.mms.Messaging");
    anAppObj.put(TAGNAME_ACCESS_CATE_ID, cateId);
    appsObj.put(anAppObj);

    anAppObj = new JSONObject();
    anAppObj.put(TAGNAME_APP_NAME, "Alarmclock");
    anAppObj.put(TAGNAME_APP_PKGNAME, "com.android.alarmclock");
    anAppObj.put(TAGNAME_APP_CLASSNAME, "com.android.alarmclock.Alarmclock");
    anAppObj.put(TAGNAME_ACCESS_CATE_ID, cateId);
    appsObj.put(anAppObj);
    
    anAppObj = new JSONObject();
    anAppObj.put(TAGNAME_APP_NAME, "DeskClock");
    anAppObj.put(TAGNAME_APP_PKGNAME, "com.android.deskclock");
    anAppObj.put(TAGNAME_APP_CLASSNAME, "com.android.deskclock.DeskClock");
    anAppObj.put(TAGNAME_ACCESS_CATE_ID, cateId);
    appsObj.put(anAppObj);

    anAppObj = new JSONObject();
    anAppObj.put(TAGNAME_APP_NAME, "Browser");
    anAppObj.put(TAGNAME_APP_PKGNAME, "com.android.browser");
    anAppObj.put(TAGNAME_APP_CLASSNAME, "com.android.browser.Browser");
    anAppObj.put(TAGNAME_ACCESS_CATE_ID, cateId);
    appsObj.put(anAppObj);
    
    anAppObj = new JSONObject();
    anAppObj.put(TAGNAME_APP_NAME, "开心网");
    anAppObj.put(TAGNAME_APP_PKGNAME, "com.kaixin001.activity");
//    anAppObj.put(TAGNAME_APP_CLASSNAME, "com.kaixin001.activity");
    anAppObj.put(TAGNAME_ACCESS_CATE_ID, cateId);
    appsObj.put(anAppObj);   
    
//    anAppObj = new JSONObject();
//    anAppObj.put(TAGNAME_APP_NAME, "植物大战僵尸");
//    anAppObj.put(TAGNAME_APP_PKGNAME, "com.popcap.pvz");
////    anAppObj.put(TAGNAME_APP_CLASSNAME, "com.kaixin001.activity");
//    anAppObj.put(TAGNAME_ACCESS_CATE_ID, cateId);
//    appsObj.put(anAppObj);     
    
    return appsObj;
  }
  
  private void loadCateDefFromFile(JSONArray catesAry, JSONArray appsAry) { 
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder dbd = dbf.newDocumentBuilder();
      Document doc = dbd.parse(cateDefFile);
      Element rootElem = doc.getDocumentElement();   

      NodeList catesList = rootElem.getElementsByTagName(TAGNAME_ACCESS_CATEGORY);
      if (catesList!=null && catesList.getLength()>0) {
        for (int i=0; i<catesList.getLength(); i++) {
          Element cateElem = (Element)catesList.item(i);

          JSONArray rulesAry = new JSONArray();
          NodeList rulesList = cateElem.getElementsByTagName(TAGNAME_ACCESS_RULE);
          for (int k=0; k<rulesList.getLength(); k++) {
            Element ruleElem = (Element)rulesList.item(k);
            
            JSONObject aRuleObj = new JSONObject();
            NamedNodeMap attrs = ruleElem.getAttributes();
            if (attrs != null) {
              String attrVal = attrs.getNamedItem(TAGNAME_RULE_AUTH_TYPE).getNodeValue();
              if (attrVal.equalsIgnoreCase(TXT_ACCESS_TYPE_DENIED)) {
                aRuleObj.put(TAGNAME_RULE_AUTH_TYPE, ACCESS_TYPE_DENIED);
              } else if (attrVal.equalsIgnoreCase(TXT_ACCESS_TYPE_PERMITTED)) {
                aRuleObj.put(TAGNAME_RULE_AUTH_TYPE, ACCESS_TYPE_PERMITTED);
              }
              
              attrVal = attrs.getNamedItem(TAGNAME_RULE_REPEAT_TYPE).getNodeValue();
              if (attrVal.equalsIgnoreCase(TXT_RECUR_TYPE_DAILY)) {
                aRuleObj.put(TAGNAME_RULE_REPEAT_TYPE, RECUR_TYPE_DAILY);
                
                String repeatStr = attrs.getNamedItem(TAGNAME_RULE_REPEAT_VALUE).getNodeValue();
                aRuleObj.put(TAGNAME_RULE_REPEAT_VALUE, 0);
                
              } else if (attrVal.equalsIgnoreCase(TXT_RECUR_TYPE_WEEKLY)) {
                aRuleObj.put(TAGNAME_RULE_REPEAT_TYPE, RECUR_TYPE_WEEKLY);
                
                int repeatVal = 0;
                String repeatStr = attrs.getNamedItem(TAGNAME_RULE_REPEAT_VALUE).getNodeValue();
                StringTokenizer repeatTokens = new StringTokenizer(repeatStr, ",");
                while (repeatTokens.hasMoreTokens()) {
                  repeatStr = repeatTokens.nextToken();
                  int repeatDay = 0;
                  if (repeatStr.equalsIgnoreCase("SUNDAY")) {
                    repeatDay = Calendar.SUNDAY;
                  } else if (repeatStr.equalsIgnoreCase("MONDAY")) {
                    repeatDay = Calendar.MONDAY;
                  } else if (repeatStr.equalsIgnoreCase("TUESDAY")) {
                    repeatDay = Calendar.TUESDAY;
                  } else if (repeatStr.equalsIgnoreCase("WEDNESDAY")) {
                    repeatDay = Calendar.WEDNESDAY;
                  } else if (repeatStr.equalsIgnoreCase("THURSDAY")) {
                    repeatDay = Calendar.THURSDAY;
                  } else if (repeatStr.equalsIgnoreCase("FRIDAY")) {
                    repeatDay = Calendar.FRIDAY;                
                  } else if (repeatStr.equalsIgnoreCase("SATURDAY")) {
                    repeatDay = Calendar.SATURDAY;
                  }
                  
                  if (repeatDay > 0) repeatVal |= (1 << (repeatDay-1) );
                }
                aRuleObj.put(TAGNAME_RULE_REPEAT_VALUE, repeatVal);  

              } if (attrVal.equalsIgnoreCase(TXT_RECUR_TYPE_MONTHLY)) {
                aRuleObj.put(TAGNAME_RULE_REPEAT_TYPE, RECUR_TYPE_MONTHLY);
              } if (attrVal.equalsIgnoreCase(TXT_RECUR_TYPE_YEARLY)) {
                aRuleObj.put(TAGNAME_RULE_REPEAT_TYPE, RECUR_TYPE_YEARLY);
              }
              
              JSONArray trsAry = new JSONArray();
              NodeList trList = ruleElem.getElementsByTagName(TAGNAME_ACCESS_TIMERANGE);
              for (int m=0; m<trList.getLength(); m++) {
                Element trElem = (Element)trList.item(m);
                attrs = trElem.getAttributes();
                if (attrs != null) {
                  JSONObject aTrObj = new JSONObject();
                  String trVal = attrs.getNamedItem(TAGNAME_RULE_REPEAT_STARTTIME).getNodeValue();
                  aTrObj.put(TAGNAME_RULE_REPEAT_STARTTIME, trVal);
                  trVal = attrs.getNamedItem(TAGNAME_RULE_REPEAT_ENDTIME).getNodeValue();
                  aTrObj.put(TAGNAME_RULE_REPEAT_ENDTIME, trVal);
                  
                  trsAry.put(aTrObj);
                }
              }
              aRuleObj.put(TAGNAME_ACCESS_TIMERANGES, trsAry);
              
            }//ruleElem' attrs
            
            rulesAry.put(aRuleObj);
            
          }//rulesList
          
          JSONObject aCateObj = new JSONObject();
          NamedNodeMap cateAttrs = cateElem.getAttributes();
          if (cateAttrs != null) {
            aCateObj.put(TAGNAME_ACCESS_CATE_ID, cateAttrs.getNamedItem(TAGNAME_ACCESS_CATE_ID).getNodeValue());
            aCateObj.put(TAGNAME_ACCESS_CATE_NAME, cateAttrs.getNamedItem(TAGNAME_ACCESS_CATE_NAME).getNodeValue());
          }
          aCateObj.put(TAGNAME_ACCESS_RULES, rulesAry);
          
          catesAry.put(aCateObj);
        }
      }//catesList
      
      NodeList appsList = rootElem.getElementsByTagName(TAGNAME_APP);
      if (appsList!=null && appsList.getLength()>0) {
        for (int i=0; i<appsList.getLength(); i++) {
          Element appElem = (Element)appsList.item(i);

          JSONObject anAppObj = new JSONObject();
          NamedNodeMap attrs = appElem.getAttributes();
          if (attrs != null) {
            anAppObj.put(TAGNAME_APP_NAME, attrs.getNamedItem(TAGNAME_APP_NAME).getNodeValue());
            anAppObj.put(TAGNAME_APP_PKGNAME, attrs.getNamedItem(TAGNAME_APP_PKGNAME).getNodeValue());
            anAppObj.put(TAGNAME_APP_CLASSNAME, attrs.getNamedItem(TAGNAME_APP_CLASSNAME).getNodeValue());
            anAppObj.put(TAGNAME_ACCESS_CATE_ID, attrs.getNamedItem(TAGNAME_ACCESS_CATE_ID).getNodeValue());
            appsAry.put(anAppObj);
          }
        }
      }//appsList
      
      logger.info("Load Category Definition From File over!");
      
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
  ///////////////////////////////////////////////////
  public static void main(String[] args) {
    System.out.println("enter WsClientCli's main()!");
    
    WsClientCli client = new WsClientCli();
    client.parse_args(args);
    client.execute();
  }

}