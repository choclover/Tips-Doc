
package studentpal.model.connection;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConnectionManager {
  final static Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
  
  private static HashMap<String, Object> phoneConnMap = new HashMap<String, Object>();
  
  public static synchronized void addConnection(String phoneNo, PhoneConnection pconn) {
    logger.info("Adding Connection for mobileNo: "+phoneNo);
    
    if (phoneConnMap.containsKey(phoneNo)) {
      phoneConnMap.remove(phoneNo);
      phoneConnMap.put(phoneNo, pconn);
    } else {
      phoneConnMap.put(phoneNo, pconn);
    }
  }
  
  public static synchronized void removeConnection(String phoneNo) {
    if (phoneConnMap.containsKey(phoneNo)) {
      logger.info("Closing and Removing connection for mobileNo: "+phoneNo);
      phoneConnMap.remove(phoneNo);
    } else {
      logger.warn("Connection for mobileNo("+phoneNo+ ") NOT found!");
    }
  }
  
  public static void removeConnection(PhoneConnection pconnToRemove) {
    String phoneNo = pconnToRemove.getBoundPhoneNo();
    if (phoneNo != null) {
      removeConnection(phoneNo);
    }
  }
  
  public static synchronized PhoneConnection retrieveConnection(String phoneNo) {
    logger.info("Retrieving Connection for mobileNo: "+phoneNo);
    
    PhoneConnection result = null;
    if (phoneConnMap.containsKey(phoneNo)) {
      result = (PhoneConnection)phoneConnMap.get(phoneNo);
    }
    return result;
  }
}
