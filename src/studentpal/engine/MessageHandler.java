package studentpal.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import studentpal.model.message.Message;
import studentpal.model.task.TaskFactory;

public class MessageHandler implements Runnable {
  private final static Logger logger = LoggerFactory.getLogger(MessageHandler.class);
  
  private boolean stopped = false;
  private ServerEngine engine;
  
  public MessageHandler(ServerEngine coreapp) {
    this.engine = coreapp;
    this.stopped = false;
    
    Thread handler = new Thread(this);
    handler.start();
  }
  
  public void run() {
    while (!stopped) {
      try {
        Message evnt = EvntQueue.get();
        if (evnt != null) {
          logger.info("Event arrived: ", evnt.getType());
          try {
            handleEvent(evnt.getType(), evnt.getCode(), evnt.getData());
          } catch (Exception exp) {
            exp.printStackTrace();
          }
          EvntPool.put(evnt);
        }
        
        Thread.sleep(100);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  public void terminate() {
    this.stopped = true;
  }
  
  public void handleEvent(int respType, int respCode, Object respBody) {
    //TODO
  }
  
    
}
