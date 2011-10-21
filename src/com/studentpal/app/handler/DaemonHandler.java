package com.studentpal.app.handler;

import android.os.Messenger;

import com.studentpal.engine.AppHandler;
import com.studentpal.engine.ClientEngine;

public class DaemonHandler implements AppHandler {
  private static final String TAG = "@@ DaemonHandler";
  
  /*
   * Field members
   */
  private static DaemonHandler instance = null;
  private ClientEngine  engine = null;
  private MessageHandler msgHandler = null;
  private Messenger mMessenger = null; 
  
  private DaemonHandler() {
  }
  
  public static DaemonHandler getInstance() {
    if (instance == null) {
      instance  = new DaemonHandler();
    }
    return instance;
  }
  
  @Override
  public void launch() {
    initialize();
  }

  @Override
  public void terminate() {
    // TODO Auto-generated method stub

  }

  //////////////////////////////////////////////////////////////////////////////
  private void initialize() {
    this.engine = ClientEngine.getInstance();
    this.msgHandler = engine.getMsgHandler();
    
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    mMessenger = new Messenger(msgHandler);
    
  }
}
