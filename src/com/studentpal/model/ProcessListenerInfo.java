package com.studentpal.model;

import static com.studentpal.app.listener.ProcessListener.PROCESS_IS_BACKGROUND;
import static com.studentpal.app.listener.ProcessListener.PROCESS_IS_FOREGROUND;

import java.util.HashSet;
import java.util.Set;

import com.studentpal.app.listener.ProcessListener;
import com.studentpal.model.exception.STDException;
import com.studentpal.util.Utils;
import com.studentpal.util.logger.Logger;

public class ProcessListenerInfo {
  private static final String TAG = "@@ ProcessListenerInfo";
  
  private int _state = PROCESS_IS_BACKGROUND;
  private Set<ProcessListener> _listeners;
  private Set<String>          _listenedProcesses;
  
  public ProcessListenerInfo() {
    if (_listeners == null) {
      _listeners = new HashSet<ProcessListener>();
    }
    if (_listenedProcesses == null) {
      _listenedProcesses = new HashSet<String>();
    }
  }
  
  public Set<ProcessListener> getListener() {
    return _listeners;
  }
  
  public String getListenedProcessStr() {
    String result = "";
    for (String procName : _listenedProcesses) {
      result += procName + ";";
    }
    return result;
  }
  
  public void addListener(ProcessListener listener) throws STDException {
    if (listener == null) {
      throw new STDException("Process Listener should NOT be NULL!");
    }
    _listeners.add(listener);
  }
  
  public void removeListener(ProcessListener listener) throws STDException {
    if (listener == null) {
      throw new STDException("Process Listener should NOT be NULL!");
    }
    
    if (_listeners == null || false == _listeners.contains(listener)) {
      throw new STDException("Process Listener is NEVER registered!");
    }
    _listeners.remove(listener);
  }
  
  public void addProcess(String procName) /*throws STDException*/ {
    if (Utils.isEmptyString(procName)) {
      Logger.d(TAG, "Process Name should NOT be NULL!");
    }
    _listenedProcesses.add(procName);
  }
  
  public void removeProcess(String procName) /*throws STDException*/ {
    if (Utils.isEmptyString(procName)) {
      Logger.d(TAG, "Process Name should NOT be NULL!");
    }
    
    if (_listenedProcesses == null || false == _listenedProcesses.contains(procName)) {
      Logger.d(TAG, "Process Name is NEVER registered!");
    }
    _listenedProcesses.remove(procName);
  }
  
  public void notifyProcessIsForeground(boolean isForeground, String procName) {
    for (ProcessListener listener : _listeners) {
      if (listener != null) {
        listener.notifyProcessIsForeground(isForeground, procName);
      }
    }
  }
  
  public boolean processIsListened(String procName) {
    if (Utils.isEmptyString(procName)) {
      Logger.d(TAG, "Process Name should NOT be NULL!");
    }
    
    boolean result = _listenedProcesses.contains(procName);
    return result;
  }
  
  
  public void setToForegroundState(boolean bFG) {
    Logger.d(TAG, "Foreground stat is set to: "+bFG);
    _state = bFG ? PROCESS_IS_FOREGROUND : PROCESS_IS_BACKGROUND;
  }
  
  public boolean isForegroundState() {
    return (_state == PROCESS_IS_FOREGROUND);
  }
  
}//class ProcessListenerInfo

