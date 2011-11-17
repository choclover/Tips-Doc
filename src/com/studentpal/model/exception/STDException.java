package com.studentpal.model.exception;

public class STDException extends Exception {
  private String desc;
  
  public STDException(String desc) {
    this.desc = desc;
  }
  
  @Override
  public String getMessage() {
    return desc;
  }
}
