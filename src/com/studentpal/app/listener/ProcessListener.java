package com.studentpal.app.listener;

public interface ProcessListener {
  
  public static int PROCESS_IS_FOREGROUND = 1;
  public static int PROCESS_IS_BACKGROUND = 2;
  
  public void notifyProcessIsForeground(boolean isForeground, String procName);
}
