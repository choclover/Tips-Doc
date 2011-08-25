package studentpal.util;

public class Utils {
  
  public static boolean isValidPhoneNumber(String phoneNum) {
    return (phoneNum!=null && phoneNum.length()==11);
  }
}
