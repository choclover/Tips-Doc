package studentpal.app.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import studentpal.app.MessageHandler;
import studentpal.engine.AppHandler;
import studentpal.engine.ClientEngine;
import studentpal.engine.Message;
import studentpal.engine.request.Request;
import studentpal.model.exception.STDException;
import studentpal.util.logger.Logger;

import android.util.Log;

public class IoHandler implements AppHandler {
  
  private static final String TAG = "IoHandler";
  private static final int SLEEP_TIME = 100;  //mill-seconds
  
  /*
   * Field members
   */
  private ClientEngine  engine = null;
  private static IoHandler instance = null;
  private MessageHandler msgHandler = null;
  
  private RemoteConnectionThread connection  = null;

  /*
   * Methods
   */
  private IoHandler() {
    initialize();
  }
  
  public static IoHandler getInstance() {
    if (instance == null) {
      instance = new IoHandler();
      
    }
    return instance;
  }

  private void initialize() {
    //TODO
  }
  
  public void launch() {
    this.engine = ClientEngine.getInstance();  
    this.msgHandler = MessageHandler.getInstance();
    
    connection = new RemoteConnectionThread(); 
    connection.start();    
  }
  
  //////////////////////////////////////////////////////////////////////////////
  public String getRemoteSvrAddr() {
    // TODO read from config
    String addr = "192.168.10.250";
    addr = "192.168.10.108";
    return addr;
  }

  public int getRemoteSvrPort() {
    return 9123;
  }

  public String getEncoding() {
    return "UTF-8";
  }
  
  public void sendMessage(String msg) {
    try {
      
    }
  }
  throws STDException
  //////////////////////////////////////////////////////////////////////////////
  //Connection thread
  class RemoteConnectionThread extends Thread {
    private static final String TAG = "RemoteConnectionThread";
    
    private boolean stopped = false;
    private BufferedInputStream bis;
    private BufferedOutputStream bos;
    private Socket socket;
    
    public void terminate() {
      stopped = true;
      this.interrupt();
      
      if (bis != null) 
        try { bis.close(); } 
        catch (IOException e) { Logger.w(TAG, e.toString()); }
      
      if (bos != null)  
        try { bos.close(); } 
        catch (IOException e) { Logger.w(TAG, e.toString()); }
        
      if (socket != null) 
        try { socket.close(); } 
        catch (IOException e) { Logger.w(TAG, e.toString()); }
    }

    public void sendMessage(String msg) throws STDException {
      if (bos == null) {
        throw new STDException("Output stream should NOT be null!");
      }
      
      try {
        byte[] msgBytes;
        msgBytes = msg.getBytes(getEncoding());
        
        bos.write(msgBytes);
        bos.flush();
        
      } catch (UnsupportedEncodingException e) {
        Logger.w(TAG, e.toString());        
      } catch (IOException e) {
        Logger.w(TAG, e.toString());
      }
    }
    
    public void run() {
      try {
        // The IP here should NOT be localhost which is the phone itself
        socket = new Socket(InetAddress.getByName(getRemoteSvrAddr()), getRemoteSvrPort());
        socket.setKeepAlive(true);
        bis = new BufferedInputStream(socket.getInputStream());
        bos = new BufferedOutputStream(socket.getOutputStream());

        byte[] buffer = null;
        String reqType = "";
        
        while (! stopped ) {
          if (bis.available() > 0) {
            buffer = new byte[bis.available()];
            bis.read(buffer);
            
            String reqStr = new String(buffer, getEncoding());
            Logger.i(TAG, "AndrClient Got request:\n"+reqStr);

            try {
              JSONObject reqRoot = new JSONObject(reqStr);

              String msgType = reqRoot.getString(Message.TAGNAME_MSG_TYPE);
              if (msgType.equals(Message.MESSAGE_HEADER_REQ)) {
                reqType = reqRoot.getString(Message.TAGNAME_CMD_TYPE);
                String reqClazName = Request.class.getName() + reqType;
                Request request;
                request = (Request)Class.forName(reqClazName).newInstance();
                
                if (request != null) {
                  msgHandler.sendRequest(request);
                }
              }
            } catch (JSONException ex) {
              Logger.w(TAG, "JSON paring error for request:\n" + reqStr);
            } catch (InstantiationException ex) {
              Logger.w(TAG, "Unsupported REQUEST type("+reqType+") in this version.");
            } catch (IllegalAccessException e) {
              Logger.w(TAG, e.toString());
            } catch (ClassNotFoundException e) {
              Logger.w(TAG, e.toString());
            }
          }

          Thread.sleep(SLEEP_TIME);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}