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
import com.studentpal.engine.Message;
import com.studentpal.engine.request.Request;
import com.studentpal.model.codec.Codec;
import com.studentpal.model.exception.STDException;
import com.studentpal.util.logger.Logger;

import android.os.Handler;
import android.os.HandlerThread;


public class IoHandler implements AppHandler {
  
  private static final String TAG = "IoHandler";
  private static final int SLEEP_TIME = 100;  //mill-seconds
  
  /*
   * Field members
   */
  private ClientEngine  engine = null;
  private static IoHandler instance = null;
  private MessageHandler msgHandler = null;
  
  private RemoteConnectionThread remoConnThread  = null;

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
    
    remoConnThread = new RemoteConnectionThread(); 
    remoConnThread.start();    
  }
  
  @Override
  public void terminate() {
    // TODO Auto-generated method stub
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
  
  public void sendMsgStr(String msg) {
    try {
      if (remoConnThread != null) {
        remoConnThread.sendMsgStr(msg);
      } else {
        Logger.w(TAG, "IO connection is NULL");
      }

    } catch (STDException e) {
      Logger.w(TAG, "SendMsgStr() got error of "+e.toString());
    }
  }
  
  public void handleResponseMessage(JSONObject msgObj) {
    
  }
  
  //////////////////////////////////////////////////////////////////////////////
  //Connection thread
  class RemoteConnectionThread extends HandlerThread {
    private static final String TAG = "RemoteConnectionThread";

    private boolean isStop = false;
    private boolean isLogin = false;  //TODO how to use this flag in client?
    
    private BufferedInputStream bis = null;
    private BufferedOutputStream bos = null;
    private Socket socket = null;

    private Handler outputMsgHandler = null;

    public RemoteConnectionThread() {
      super(TAG);
    }

    public void terminate() {
      isStop = true;
      this.interrupt();

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

      if (socket != null)
        try {
          socket.close();
        } catch (IOException e) {
          Logger.w(TAG, e.toString());
        }
        
      if (outputMsgHandler != null) {
        outputMsgHandler.removeMessages(0);
        outputMsgHandler = null;
      }
    }

    public void run() {
      try {
        socket = constructSocketConnection();
        bis = new BufferedInputStream(socket.getInputStream());
        bos = new BufferedOutputStream(socket.getOutputStream());
        isLogin = true;

        /*
         * Output message handler
         */
        this.outputMsgHandler = new Handler(this.getLooper()) {
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

        // Start to login server
        try {
          engine.loginServer();
        } catch (STDException e1) {
          Logger.w(TAG, e1.toString());
        }

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
            String reqType = null;
            
            try {
              JSONObject msgObjRoot = new JSONObject(msgStr);

              String msgType = msgObjRoot.getString(Message.TAGNAME_MSG_TYPE);
              if (msgType.equals(Message.MESSAGE_HEADER_REQ)) {
                reqType = msgObjRoot.getString(Message.TAGNAME_CMD_TYPE);
                String reqClazName = Request.class.getName() + reqType;
                Request request;
                request = (Request) Class.forName(reqClazName).newInstance();

                if (request != null) {
                  // send incoming request to MessageHandler to handle
                  msgHandler.sendRequest(request);
                }
                
              } else if (msgType.equals(Message.MESSAGE_HEADER_ACK)) {
                handleResponseMessage(msgObjRoot);
                
              } else {
                Logger.i(TAG, "Unsupported Incoming MESSAGE type(" + msgType
                    + ") in this version.");
              }
              
            } catch (JSONException ex) {
              Logger.w(TAG, "JSON paring error for request:\n" + msgStr);
            } catch (InstantiationException ex) {
              Logger.w(TAG, "Unsupported REQUEST type(" + reqType
                  + ") in this version.");
            } catch (IllegalAccessException e) {
              Logger.w(TAG, e.toString());
            } catch (ClassNotFoundException e) {
              Logger.w(TAG, e.toString());
            }
          }

          Thread.sleep(SLEEP_TIME);
        }// while !stopped

      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void sendMsgStr(String msgStr) throws STDException {
      if (outputMsgHandler == null) {
        throw new STDException("Output Msg handler should NOT be null!");
      }
      android.os.Message msg = this.outputMsgHandler.obtainMessage(0, msgStr);
      this.outputMsgHandler.sendMessage(msg);
    }

    private void sendMsgStr_internal(String msg) throws STDException {
      if (bos == null) {
        throw new STDException("Output stream should NOT be null!");
      }

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

    private Socket constructSocketConnection() {
      Socket socket = null;
      try {
        // The IP here should NOT be localhost which is the phone itself
        socket = new Socket(InetAddress.getByName(getRemoteSvrAddr()),
            getRemoteSvrPort());
        socket.setKeepAlive(true);
        // TODO handle re-connnect and exception handling

      } catch (UnknownHostException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      return socket;
    }
    
  }// class RemoteConnectionThread

}//class IoHandler



