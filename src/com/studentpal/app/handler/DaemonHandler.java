package com.studentpal.app.handler;

import static com.studentpal.engine.Event.SIGNAL_TYPE_DAEMON_WD_REQ;
import static com.studentpal.engine.Event.SIGNAL_TYPE_DAEMON_WD_RESP;
import static com.studentpal.engine.Event.SIGNAL_TYPE_DAEMON_WD_TIMEOUT;
import static com.studentpal.engine.Event.SIGNAL_TYPE_START_DAEMONTASK;
import static com.studentpal.engine.Event.SIGNAL_TYPE_STOP_DAEMONTASK;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.studentpal.app.ResourceManager;
import com.studentpal.engine.AppHandler;
import com.studentpal.engine.ClientEngine;
import com.studentpal.engine.Event;
import com.studentpal.util.ActivityUtil;
import com.studentpal.util.Utils;
import com.studentpal.util.logger.Logger;

public class DaemonHandler implements AppHandler {
  private static final String TAG = "@@ DaemonHandler";
  
  /*
   * Constants
   */
  public static final String ACTION_DAEMON_SVC = "studentpal.daemon";
  
  public static final int DAEMON_WATCHDOG_INTERVAL = 500;   //milliseconds为单位
  public static final int DAEMON_WATCHDOG_TIMEOUT  = DAEMON_WATCHDOG_INTERVAL * 2; 
  
  /*
   * Field members
   */
  private static DaemonHandler instance          = null;
  private ClientEngine         engine            = null;
  private Handler              msgHandler        = null;
  private Context              launcher          = null;

  // Flag indicating whether I have bound to Daemon task
  private boolean bBoundToDaemon = false;
  /* Messenger for communicating with Daemon service. */
  private Messenger mMsgerToDaemon = null;
  /* Messenger for Daemon service communicating with me. */
  private Messenger mMsgerToMyself = null;
  /* Connection for interacting with the main interface of the service. */
  private ServiceConnection mSvcConnection = null;
  
  //////////////////////////////////////////////////////////////////////////////
  private DaemonHandler() {
  }

  public static DaemonHandler getInstance() {
    if (instance == null) {
      instance = new DaemonHandler();
    }
    return instance;
  }

  @Override
  public void launch() {
    initialize();
    launchDaemonService();
  }

  @Override
  public void terminate() {
    ((IncomingHandler) this.msgHandler).terminate();
    doUnbindService();
  }

  
  public void sendMsgToDaemon(int msgtype) throws RemoteException {
    if (mMsgerToDaemon == null) {
      Logger.w(TAG, "Messanger To Daemon should NOT be NULL!");
      throw new RemoteException();
    }
    Message msg = Message.obtain(null, msgtype);
    msg.replyTo = mMsgerToMyself;
    mMsgerToDaemon.send(msg);
  }
  
  // ///////////////////////////////////////////////////////////////////////////
  private void initialize() {
    this.engine = ClientEngine.getInstance();
    this.launcher = engine.getContext();
    this.msgHandler = new IncomingHandler();
    
    /**
     * Target we publish for Daemon service to send messages to MessageHandler/myself.
     */
    mMsgerToMyself = new Messenger(msgHandler);
    mSvcConnection = new MyServiceConnection();
  }
  
  private void launchDaemonService() {
    //no matter if Daemon service is running or not,
    //start it anyway and bind to it next.
    ActivityUtil.startDaemonService(launcher);
    Utils.sleep(200);  //FIXME -- maybe not useful
    doBindService();  
  }

  void doBindService() {
    Logger.d(TAG, "Binding to Daemon task!");
    
    // Establish a connection with the service.
    launcher.bindService(new Intent(ACTION_DAEMON_SVC), 
        mSvcConnection, Context.BIND_AUTO_CREATE);
    bBoundToDaemon = true;
  }

  void doUnbindService() {
    Logger.d(TAG, "Unbinding from Daemon task!");
    
    if (bBoundToDaemon) {
      // If we have received the service, and hence registered with
      // it, then now is the time to unregister.
      if (mMsgerToDaemon != null) {
        try {
          sendMsgToDaemon(Event.SIGNAL_TYPE_STOP_DAEMONTASK);
        } catch (RemoteException e) {
          // There is nothing special we need to do if the service
          // has crashed.
        }
      }
      
      if (mSvcConnection != null) {  
        // Detach our existing connection.
        launcher.unbindService(mSvcConnection);
      }
      
      bBoundToDaemon = false;
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  class MyServiceConnection implements ServiceConnection {
    public void onServiceConnected(ComponentName className, IBinder service) {
      Logger.d(TAG, "Connected to Daemon service @ "+service);

      // This is called when the connection with the service has been
      // established, giving us the service object we can use to
      // interact with the service. We are communicating with our
      // service through an IDL interface, so get a client-side
      // representation of that from the raw service object.
      mMsgerToDaemon = new Messenger(service);

      // We want to monitor the service for as long as we are
      // connected to it.
      try {
        sendMsgToDaemon(Event.SIGNAL_TYPE_START_DAEMONTASK);
      } catch (RemoteException e) {
        // In this case the service has crashed before we could even
        // do anything with it; we can count on soon being
        // disconnected (and then reconnected if it can be restarted)
        // so there is no need to do anything here.
      }
    }

    public void onServiceDisconnected(ComponentName className) {
      // This is called when the connection with the service has been
      // unexpectedly disconnected -- that is, its process crashed.
      Logger.d(TAG, "Disconnected from Daemon service @ ");
      
      mMsgerToDaemon = null;
      doUnbindService();
    }
  };
  
  /**
   * Handler of incoming messages from clients.
   */
  class IncomingHandler extends Handler {
    //private static final String TAG = "DaemonHandler.IncomingHandler";
    
    void terminate() {
      Logger.d(TAG, "IncomingHandler.terminate()!");
      removeMessages(SIGNAL_TYPE_DAEMON_WD_TIMEOUT);
    }
    
    @Override
    public void handleMessage(Message msg) {
      int sigType = msg.what;
      //Logger.v(TAG, "Got msg of type: "+sigType);
      switch (sigType) {
      /*
       * Signal between DAEMON task
       */
      case SIGNAL_TYPE_DAEMON_WD_REQ:
        removeMessages(SIGNAL_TYPE_DAEMON_WD_TIMEOUT);
        try {
          sendMsgToDaemon(SIGNAL_TYPE_DAEMON_WD_RESP);
        } catch (RemoteException e) {
          Logger.w(TAG, e.toString());
        }
        sendEmptyMessageDelayed(SIGNAL_TYPE_DAEMON_WD_TIMEOUT,
            DaemonHandler.DAEMON_WATCHDOG_TIMEOUT);
        break;

      case SIGNAL_TYPE_DAEMON_WD_TIMEOUT:
        Logger.w(TAG, "Waiting for Daemon Watchdog request has timeout!");
        if (false == ActivityUtil.isServiceRunning(launcher,
            ResourceManager.DAEMON_SVC_PKG_NAME)) {
          Logger.w(TAG, "Daemon is NOT running, relaunching it!");
          launchDaemonService();

        } else {
          Logger.w(TAG, "Daemon is still running.");
        }
        break;

      default:
        super.handleMessage(msg);

      }
    }
  }
}
