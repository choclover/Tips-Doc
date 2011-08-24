package studentpal.test.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import studentpal.engine.ServerEngine;
import studentpal.model.codec.AsCodecFactory;
import studentpal.model.message.Message;
import studentpal.model.task.TaskDefinition;
import studentpal.model.task.TaskFactory;

//import org.apache.cxf.endpoint.Client;
//import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;

public class PhoneClientCli {
  BufferedWriter bwriter;
  BufferedReader bdis;
  BufferedInputStream bis;
  BufferedOutputStream bos;

  static String phone_number1 = "155";
  static String phone_number2 = "5521";
  static String phone_number3 = "5554";

  boolean loggined = false;
  boolean bDebug = false;
  String phoneNo = "";
  
  public void sockConnect() {
    try {
      Socket sock = new Socket(InetAddress.getLocalHost(),
          ServerEngine.SVR_PORT);

      bis = new BufferedInputStream(sock.getInputStream());
      bos = new BufferedOutputStream(sock.getOutputStream());

      Thread readerThd = new Thread(new Runnable() {
        // ByteBuffer buffer = ByteBuffer.allocate(1024 * 20);
        byte[] buffer = new byte[1024 * 20];

        @Override
        public void run() {
          try {
            while (true) {
              if (bis.available() > 0) {
                bis.read(buffer);
                String cmd = new String(buffer, AsCodecFactory.CHARSET_NAME).trim();
                System.out.println("Client Got request:\n\t" + cmd);
                
                handleRequestStr(cmd);
              }
              Thread.sleep(1000);
            }
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          } catch (InterruptedException e) {
            e.printStackTrace();            
          }
        }
      });
      readerThd.start();

      while (true) {
        System.out.println("Please input a command: ");
        String cmd = new BufferedReader(new InputStreamReader(System.in))
            .readLine();
        if (cmd.isEmpty()) continue;

        String msg = getRequestStr(cmd.trim());
        if (msg!=null && !msg.isEmpty()) {
          sendMessage(msg);
        }
      }
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getRequestStr(String cmd) {
    String msg = null;
    
    try {
      JSONObject root = new JSONObject();
      root.put(Message.TAGNAME_MSG_TYPE, Message.MESSAGE_HEADER_REQ);
      
      if (cmd.equalsIgnoreCase("login")) {
        root.put(Message.TAGNAME_CMD_TYPE, Message.TASKNAME_LOGIN);
        
        JSONObject args = new JSONObject();
        this.phoneNo = new StringBuffer().append(phone_number1).append(
            phone_number2).append(phone_number3).toString();
        phone_number3 = String.valueOf(Integer.valueOf(phone_number3).intValue()+1);
        args.put(Message.TAGNAME_PHONE_NUM, phoneNo);

        root.put(Message.TAGNAME_ARGUMENTS, args);
        loggined = true;
        
      } else if (cmd.equalsIgnoreCase("logout") && loggined) {
        root.put(Message.TAGNAME_CMD_TYPE, Message.TASKNAME_LOGOUT);
        JSONObject args = new JSONObject();
        args.put(Message.TAGNAME_PHONE_NUM, phoneNo);
        
        root.put(Message.TAGNAME_ARGUMENTS, args);
        loggined = false;
        
      } else {
        return null;
      }
      
      msg = root.toString();
      
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return msg;
  }

  public void sendMessage(String msgStr) {
    try {
      byte[] outputAry = generateOutputByteAry(msgStr
          .getBytes(AsCodecFactory.CHARSET_NAME));

      bos.write(outputAry, 0, outputAry.length);
      bos.flush();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public void handleRequestStr(String reqStr) {
    if (bDebug) {
      try {
        int sleep = 40;
        System.out.println("I am ready to sleep "+sleep+ " seconds...");
        Thread.sleep(sleep * 1000);
        System.out.println("Sleep over!");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return;
    }
    
    JSONObject resp = new JSONObject();
    try {
      JSONObject req = new JSONObject(reqStr);
      String cmd_type = req.getString(Message.TAGNAME_CMD_TYPE);
      int req_seq = req.getInt(Message.TAGNAME_MSG_ID);

      resp.put(Message.TAGNAME_MSG_TYPE, Message.MESSAGE_HEADER_ACK);
      resp.put(Message.TAGNAME_MSG_ID, req_seq);
      resp.put(Message.TAGNAME_ERR_CODE, Message.ERRCODE_NOERROR);
      resp.put(Message.TAGNAME_CMD_TYPE, cmd_type);
      
      JSONObject result = new JSONObject();
      if (cmd_type.equals(Message.TASKNAME_GetAppList)) {
        JSONArray applications = new JSONArray();
        
        JSONObject app = new JSONObject();
        app.put(Message.TAGNAME_APP_NAME, "Browser");
        app.put(Message.TAGNAME_APP_CLASSNAME, "com.android.browser");
        app.put(Message.TAGNAME_APP_ACCESS_TYPE, 1);
        applications.put(app);
        
        app = new JSONObject();
        app.put(Message.TAGNAME_APP_NAME, "Alarm Clock");
        app.put(Message.TAGNAME_APP_CLASSNAME, "com.android.alarmclock");
        app.put(Message.TAGNAME_APP_ACCESS_TYPE, 2);
        applications.put(app);
        
        app = new JSONObject();
        app.put(Message.TAGNAME_APP_NAME, "Camera");
        app.put(Message.TAGNAME_APP_CLASSNAME, "com.android.camera");
        applications.put(app);
        
        result.put(Message.TAGNAME_APPLICATIONS, applications);
        
      } else if (cmd_type.equals(Message.TASKNAME_SetAppAccessCategory)) {
        //TODO
      }
      resp.put(Message.TAGNAME_RESULT, result);
      
      String resultStr = resp.toString();
      if (resultStr!=null && !resultStr.isEmpty()) {
        System.out.println("Sending back response to server:\n\t"+resultStr);
        sendMessage(resultStr);
      }
      
    } catch (JSONException e) {
      e.printStackTrace();
    }
    
  }
  
  public byte[] generateOutputByteAry(byte[] strBytes) {
    // encode the output str
    byte[] headerBytes = intToByteArray(strBytes.length);
    byte[] outByteAry = new byte[4 + strBytes.length];

    System.arraycopy(headerBytes, 0, outByteAry, 0, 4);
    System.arraycopy(strBytes, 0, outByteAry, 4, strBytes.length);

    return outByteAry;
  }

  public static final byte[] intToByteArray(int value) {
    return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16),
        (byte) (value >>> 8), (byte) value };
  }

  private void parse_args(String[] args) {
    for (String arg : args) {
      if (arg.equals("-debug")) {
        bDebug = true;
      }
    }
  }
  
  ///////////////////////////////////////////////////
  public static void main(String[] args) {
    PhoneClientCli client = new PhoneClientCli();
    client.parse_args(args);
    
    client.sockConnect();
  }
  

}