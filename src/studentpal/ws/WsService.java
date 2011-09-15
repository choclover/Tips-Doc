package studentpal.ws;

import javax.xml.ws.Endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsService {
  final Logger logger = LoggerFactory.getLogger(WsService.class);
  
  private static WsService instance = null;
  private static PhoneConnectorWs phoneConnWs = null;
  
  public static final String wsUrl = "http://localhost:8080/StudentPal/PhoneConnector";
  
  public static WsService getInstance() {
    if (instance == null) {
      instance = new WsService();
      phoneConnWs = new PhoneConnectorWs();
    }
    return instance;
  }
  
  public void publishWs() {
    logger.info("** ready to publish WS! **");
    
    Endpoint.publish(wsUrl, phoneConnWs);
  }
  
  public static void main(String[] args) {
    WsService wsServiceInst = WsService.getInstance();
    wsServiceInst.publishWs();
  }
}
