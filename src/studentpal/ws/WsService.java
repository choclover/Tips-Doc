package studentpal.ws;

import java.net.InetAddress;

import javax.xml.ws.Endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsService {
  final Logger logger = LoggerFactory.getLogger(WsService.class);
  
  private static WsService instance = null;
  private static PhoneConnectorWs phoneConnWs = null;
  
  public static final String wsLocation = "/StudentPal/PhoneConnector";
  public static final int    wsPort = 9090;
  
  public static WsService getInstance() {
    if (instance == null) {
      instance = new WsService();
      phoneConnWs = new PhoneConnectorWs();
    }
    return instance;
  }
  
  public void publishWs() {
    try {
      String wsUrl = getWsUrl();
      
      logger.info("** Prepare to publish WS @ " +wsUrl+ " **");
      Endpoint.publish(wsUrl, phoneConnWs);
      logger.info("** Completed publish WS! **");
      
    } catch (Exception ex) {
      logger.warn(ex.toString());
    }
  }
  
  public static String getWsUrl() {
    String result = "";
    try {
      String domainName = InetAddress.getLocalHost().getHostAddress();
      if (false) {
        domainName = "localhost";
        domainName = "coeustec.gicp.net";
      }
      
      result = "http://" +domainName+ ":" + wsPort + wsLocation;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    return result;
  }
  public static void main(String[] args) {
    WsService wsServiceInst = WsService.getInstance();
    wsServiceInst.publishWs();
  }
}
