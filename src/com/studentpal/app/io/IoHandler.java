package com.studentpal.app.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import com.studentpal.app.MessageHandler;
import com.studentpal.engine.AppHandler;
import com.studentpal.engine.ClientEngine;
import com.studentpal.engine.Event;
import com.studentpal.engine.request.Request;
import com.studentpal.model.codec.Codec;
import com.studentpal.model.exception.STDException;
import com.studentpal.util.logger.Logger;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


public class IoHandler implements AppHandler {
  
  private static final String TAG = "IoHandler";
  private static final int SLEEP_TIME = 100;  //mill-seconds
  
  /*
   * Field members
   */
  private static IoHandler instance = null;

  private ClientEngine  engine = null;
  private MessageHandler msgHandler = null;
  
  private Socket socketConn = null;
  private BufferedInputStream bis = null;
  private BufferedOutputStream bos = null;
  //private boolean isLogin = false;  //TODO how to use this flag in client?
  
  private InputConnectionThread inputConnThread  = null;
  private OutputConnectionThread outputConnThread  = null;

  /*
   * Methods
   */
  private IoHandler() {
  }
  
  public static IoHandler getInstance() {
    if (instance == null) {
      instance = new IoHandler();
    }
    return instance;
  }

  private void initialize() {
    Runnable r = new Runnable() {
      @Override
      public void run() {
        init_network();
      }
    };
    new Thread(r).start();
  }
  
  
  @Override
  public void launch() {
    this.engine = ClientEngine.getInstance();  
    this.msgHandler = this.engine.getMsgHandler();
    
    initialize();
  }
  
  @Override
  public void terminate() {
    if (inputConnThread != null) {
      inputConnThread.terminate();
    }
    if (outputConnThread != null) {
      outputConnThread.terminate();
    }
    
    if (bis != null)
      try {
        bis.close();
      } catch (IOException e) {
        Logger.w(TAG, e.toString());
      }

    if (bos != null)
      try {
        bos.close();
      } catch (IOException e) {
        Logger.w(TAG, e.toString());
      }

    if (socketConn != null)
      try {
        socketConn.close();
      } catch (IOException e) {
        Logger.w(TAG, e.toString());
      }
  }
  
  public String getRemoteSvrIP() {        
    /* 
     * Do NOT use localhost/127.0.0.1 which is the phone itself 
     */
    // TODO read from config
    String addr = "192.168.1.250";
    addr = "10.60.4.58";
    
    return addr;
  }

  public String getRemoteSvrDomainName() {
    // TODO read from config
    String addr = "coeustec.gicp.net";
    return addr;
  }

  public int getRemoteSvrPort() {
    return 9177;  //9123;
  }

  public String getEncoding() {
    return "UTF-8";
  }
  
  public void sendMsgStr(String msg) {
    try {
      if (outputConnThread != null) {
        outputConnThread.sendMsgStr(msg);
      } else {
        Logger.w(TAG, "IO connection is NULL");
      }
    } catch (STDException e) {
      Logger.w(TAG, "SendMsgStr() got error of "+e.getMessage());
    }
  }
  
  public void handleResponseMessage(JSONObject msgObj) {
    //TODO
  }
  
  //////////////////////////////////////////////////////////////////////////////
  private void init_network() {
    if (socketConn != null) {
      try { socketConn.close(); } 
      catch (IOException e) { e.printStackTrace(); }
      socketConn = null;
    }

    while (socketConn == null) {
      socketConn = constructSocketConnection();
      if (socketConn == null) {
        Logger.w(TAG, "Creating Socket returns NULL");
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      } 
      
      break;  //FIXME: design a re-connect or retry method
    }

    if (socketConn != null) {
      try {
        bis = new BufferedInputStream(socketConn.getInputStream());
        bos = new BufferedOutputStream(socketConn.getOutputStream());
        
        inputConnThread = new InputConnectionThread(); 
        inputConnThread.start();
        outputConnThread = new OutputConnectionThread(); 
        outputConnThread.start(); 
        
      } catch (IOException e) {
        Logger.w(TAG, e.toString());
      }
    }
  }
  
  private Socket constructSocketConnection() {
    Socket aSock = null;
    final int RETRY_TIMES = 5;
    
    for (int i=0; i<RETRY_TIMES; i++) {
      try {
        String svrAddr = getRemoteSvrDomainName();
        svrAddr = getRemoteSvrIP();
        int svrPort = getRemoteSvrPort();
        
        Logger.d(TAG, "Connecting to " + svrAddr +":"+ svrPort);
        aSock = new Socket(InetAddress.getByName(svrAddr), svrPort);
        aSock.setKeepAlive(true);
        Logger.d(TAG, "Connected to "+aSock.getInetAddress().toString());
        
      } catch (UnknownHostException e) {
        Logger.w(TAG, e.toString());
      } catch (IOException e) {
        Logger.w(TAG, e.toString());
      }
      
      if (aSock != null) break;
      
      try {
        Thread.sleep((i*10+5) * 1000);
      } catch (InterruptedException e) {
        Logger.w(TAG, e.toString());
      }
    }

    return aSock;
  }
  
  //**************************************************************************//
  //Connection thread
  class OutputConnectionThread extends Thread/*HandlerThread*/ {
    private static final String TAG = "@@ OutputConnectionThread";
    private Handler outputMsgHandler = null;
    private boolean isReady = false;
    
    public OutputConnectionThread() {
      super(TAG);
    }
    
    public void terminate() {
      this.interrupt();

      if (outputMsgHandler != null) {
        outputMsgHandler.removeMessages(0);
        outputMsgHandler = null;
      }
    }
    
//    public boolean isReady() {
//      return isReady;
//    }
    
    public void run() {
      /*
       * Output message handler
       */
      Looper.prepare();
      
      this.outputMsgHandler = new Handler(/*this.getLooper()*/) {
        @Override
        public void handleMessage(android.os.Message message) {
          String msgStr = (String) message.obj;

          try {
            sendMsgStr_internal(msgStr);
          } catch (STDException e) {
            Logger.w(TAG, e.toString());
          }
        }
      };
      
      //we can start to login server now since OUTPUT stream is ready
      Message signal = msgHandler.obtainMessage(Event.SIGNAL_TYPE_OUTSTREAM_READY);
      msgHandler.sendMessage(signal);
      
      Looper.loop();
    }
    
    private void sendMsgStr_internal(String msg) throws STDException {
      if (bos == null) {
        throw new STDException("Output stream should NOT be null!");
      }

      Logger.i("Ready to send msg:\n"+msg);
      
      try {
        byte[] msgBytes;
        msgBytes = msg.getBytes(getEncoding());
        msgBytes = Codec.encode(msgBytes);

        bos.write(msgBytes);  
        bos.flush();

      } catch (UnsupportedEncodingException e) {
        Logger.w(TAG, e.toString());
      } catch (IOException e) {
        Logger.w(TAG, e.toString());
      }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    public void sendMsgStr(String msgStr) throws STDException {
      if (this.outputMsgHandler == null) {
        throw new STDException("Output Msg handler should NOT be null!");
      }
      Message msg = this.outputMsgHandler.obtainMessage(0, msgStr);
      this.outputMsgHandler.sendMessage(msg);
    }
    
  }//class OutputConnectionThread
  
  //**************************************************************************//
  class InputConnectionThread extends Thread {
    private static final String TAG = "@@ InputConnectionThread";

    private boolean isStop = false;
//    private boolean isReady = false;
    
    public void terminate() {
      isStop = true;
      this.interrupt();
    }

//    public boolean isReady() {
//      return isReady;
//    }
    
    public void run() {
      try {
        /*
         * Input message receiver
         */
        byte[] buffer = null;

        while (!isStop) {
          if (bis.available() > 0) {
            buffer = new byte[bis.available()];
            bis.read(buffer);

            String msgStr = new String(buffer, getEncoding());
            Logger.i(TAG, "AndrClient Got a message:\n" + msgStr);
            if (msgStr==null || msgStr.trim().length()==0) {
              continue;
            }
            
            try {
              JSONObject msgObjRoot = new JSONObject(msgStr);
              String msgType = msgObjRoot.getString(Event.TAGNAME_MSG_TYPE);

              if (msgType.equals(Event.MESSAGE_HEADER_REQ)) {  //This is a incoming request
                String reqPkgName = Request.class.getName();
                if (reqPkgName.indexOf('.') != -1) {
                  reqPkgName = reqPkgName.substring(0, reqPkgName.lastIndexOf('.')+1);
                } else {
                  reqPkgName = "";
                }

                String reqType = msgObjRoot.getString(Event.TAGNAME_CMD_TYPE);
                String reqClazName = reqPkgName + reqType + "Request";
                Logger.i(TAG, "Ready to new instance of:"+reqClazName);
                
                Request request;
                request = (Request) Class.forName(reqClazName).newInstance();

                if (request != null) {
                  int msgId = msgObjRoot.getInt(Event.TAGNAME_MSG_ID);
                  request.setRequestSeq(msgId);
                  
                  if (msgObjRoot.has(Event.TAGNAME_ARGUMENTS)) {
                    String args = msgObjRoot.getString(Event.TAGNAME_ARGUMENTS);
                    request.setInputArguments(args);
                  }
                  
                  // send incoming request to MessageHandler to handle
                  msgHandler.sendRequest(request);
                }
                
              } else if (msgType.equals(Event.MESSAGE_HEADER_ACK)) {
                //This is a response message
                handleResponseMessage(msgObjRoot);
                
              } else {
                Logger.i(TAG, "Unsupported Incoming MESSAGE type(" + msgType
                    + ") in this version.");
              }
              
            } catch (JSONException ex) {
              Logger.w(TAG, "JSON paring error for request:\n\t" + msgStr);
              Logger.w(TAG, ex.toString());
            } catch (InstantiationException ex) {
              Logger.w(TAG, ex.toString());
            } catch (IllegalAccessException e) {
              Logger.w(TAG, e.toString());
            } catch (ClassNotFoundException e) {
              Logger.w(TAG, e.toString());
            }
          }

          Thread.sleep(SLEEP_TIME);
          
        }// while !stopped

      } catch (InterruptedException e) {
        Logger.w(TAG, e.toString());
      } catch (UnsupportedEncodingException e) {
        Logger.w(TAG, e.toString());
      } catch (IOException e) {
        Logger.w(TAG, e.toString());
      }
    }
    
  }// class RemoteConnectionThread

}//class IoHandler



