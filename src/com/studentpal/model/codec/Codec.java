package com.studentpal.model.codec;

import com.studentpal.util.Utils;

public class Codec {
  public static final int CODEC_HEADER_LENGTH = 4;
  
  public static byte[] encode(byte[] sour) {
    byte[] dest = new byte[sour.length + CODEC_HEADER_LENGTH];
    byte[] lenBytes = Utils.intToByteArray(sour.length);

    // 将前4位设置成数据体的字节长度
    System.arraycopy(lenBytes, 0, dest, 0, CODEC_HEADER_LENGTH);   
    // message content
    System.arraycopy(sour, 0, dest, CODEC_HEADER_LENGTH, sour.length);    
    
    return dest;
  }
  
  public static byte[] decode(byte[] src) {
    
    return null;
  }
}
