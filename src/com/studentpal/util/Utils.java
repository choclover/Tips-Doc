package com.studentpal.util;

public class Utils {

  public static boolean isValidPhoneNumber(String phoneNum) {
    return (phoneNum != null && phoneNum.trim().length() == 11);
  }

  //International Mobile Equipment Identification Number
  public static boolean isValidPhoneIMEI(String phoneIMEI) {
    return (phoneIMEI != null && phoneIMEI.trim().length() == 15);
  }
  
  //International Mobile Subscriber Identification Number
  public static boolean isValidPhoneIMSI(String phoneIMSI) {
    return (phoneIMSI != null && phoneIMSI.trim().length() == 15);
  }
  
  public static boolean isEmptyString(String str) {
    return (str==null || str.trim().length()==0);
  }
  
  public static final int byteArrayToInt(byte[] b) {
    return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8)
        + (b[3] & 0xFF);
  }

  public static final byte[] intToByteArray(int value) {
    return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16),
        (byte) (value >>> 8), (byte) value };
  }
  
  
}
