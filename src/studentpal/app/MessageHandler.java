package studentpal.app;

import studentpal.app.io.IoHandler;
import studentpal.engine.AppHandler;
import studentpal.engine.ClientEngine;
import studentpal.engine.request.Request;
import studentpal.util.logger.Logger;
import android.os.Handler;
import android.os.Message;

public class MessageHandler extends Handler implements AppHandler {
  
  private static final String TAG = "MessageHandler";
  
  private static MessageHandler instance = null;
  private IoHandler     ioHandler = null;
  private ClientEngine  engine = null;
  
  private MessageHandler() {
    initialize();
  }
  
  public static MessageHandler getInstance() {
    if (instance == null) {
      instance  = new MessageHandler();
    }
    return instance;
  }
  
  public void launch() {
    this.engine = ClientEngine.getInstance();    
    this.ioHandler = IoHandler.getInstance();
  }
  
  public void sendRequest(Request req) {
    Message msg = this.obtainMessage(0, req);
    this.sendMessage(msg);
  }
  
  @Override
  public void handleMessage(Message message) {
    Object msg = (Request)message.obj;
    
    if (msg instanceof Request) {
      Request req = (Request)msg;
      if (req.isIncomingReq()) {
        req.execute();
        this.sendRequest(req);  //add handled request to message queue again
      
      } else if (req.isOutgoingReq() && req.isReplyReady()) {
        String replyStr = req.getReplyStr();
        if (replyStr != null && replyStr.trim().length() > 0) {
          this.ioHandler.sendMessage(replyStr);
        } else {
          Logger.d(TAG, "Outgoing reply is NULL or empty for request "+req.getName());
        }
        
      } else {
        Logger.w(TAG, "Unhandled a request: "+req.getName());
      }
      
    } 
    
    super.handleMessage(message);
  }

  //////////////////////////////////////////////////////////////////////////////
  private void initialize() {
  }
}