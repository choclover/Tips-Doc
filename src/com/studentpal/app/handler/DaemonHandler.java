package com.studentpal.app.handler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.studentpal.engine.AppHandler;
import com.studentpal.engine.ClientEngine;
import com.studentpal.engine.Event;
import com.studentpal.util.ActivityUtil;
import com.studentpal.util.logger.Logger;

public class DaemonHandler implements AppHandler {
  private static final String TAG = "@@ DaemonHandler";
  
  public static final String ACTION_DAEMON_SVC = "studentpal.daemon";
  
  /*
   * Field members
   */
  private static DaemonHandler instance          = null;
  private ClientEngine         engine            = null;
  private MessageHandler       msgHandler        = null;
  private Context              launcher          = null;

  // Flag indicating whether I have bound to Daemon task
  private boolean bBoundToDaemon = false;
  /** Messenger for communicating with Daemon service. */
  private Messenger mMsgerToDaemon = null;
  /** Messenger for Daemon service communicating with me. */
  private Messenger mMsgerFromDaemon = null;

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
  }

  @Override
  public void terminate() {
    doUnbindService();
  }

  // ///////////////////////////////////////////////////////////////////////////
  private void initialize() {
    this.engine = ClientEngine.getInstance();
    this.msgHandler = engine.getMsgHandler();
    this.launcher = engine.getContext();
    
    /**
     * Target we publish for Daemon service to send messages to MessageHandler.
     */
    mMsgerFromDaemon = new Messenger(msgHandler);
  }

  /**
   * Class for interacting with the main interface of the service.
   */
  private ServiceConnection mConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder service) {
      Logger.d(TAG, "Connected to Daemon service!");

      // This is called when the connection with the service has been
      // established, giving us the service object we can use to
      // interact with the service. We are communicating with our
      // service through an IDL interface, so get a client-side
      // representation of that from the raw service object.
      mMsgerToDaemon = new Messenger(service);

      // We want to monitor the service for as long as we are
      // connected to it.
      try {
        Message msg = Message.obtain(null, Event.SIGNAL_START_DAEMONTASK);
        msg.replyTo = mMsgerFromDaemon;
        mMsgerToDaemon.send(msg);

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
      mMsgerToDaemon = null;
      Logger.d(TAG, "Disconnected from Daemon service!");
    }

  };
  
  void doBindService() {
    // Establish a connection with the service.
    launcher.bindService(new Intent(ACTION_DAEMON_SVC), 
        mConnection, Context.BIND_AUTO_CREATE);
    
    bBoundToDaemon = true;
    Logger.d(TAG, "Binding to Daemon task!");
  }

  void doUnbindService() {
    if (bBoundToDaemon) {
      // If we have received the service, and hence registered with
      // it, then now is the time to unregister.
      if (mMsgerToDaemon != null) {
        try {
          Message msg = Message.obtain(null, Event.SIGNAL_STOP_DAEMONTASK);
          msg.replyTo = mMsgerFromDaemon;
          mMsgerToDaemon.send(msg);

        } catch (RemoteException e) {
          // There is nothing special we need to do if the service
          // has crashed.
        }
      }

      // Detach our existing connection.
      launcher.unbindService(mConnection);
      
      bBoundToDaemon = false;
      Logger.d(TAG, "Unbinding from Daemon task!");
    }
  }


}
