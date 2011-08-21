
package studentpal.model.codec;


import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import studentpal.utils.Utils;

public class ResponseMsgEncoder extends ProtocolEncoderAdapter {
  private static final Logger logger = LoggerFactory.getLogger(ResponseMsgEncoder.class);
  private final Charset charset;

  public ResponseMsgEncoder(Charset charset) {
    this.charset = charset;
  }

  public void encode(IoSession session, Object message,
      ProtocolEncoderOutput out) throws Exception {
//    CharsetEncoder ce = charset.newEncoder();
    IoBuffer buffer = IoBuffer.allocate(100).setAutoExpand(true);

    String msgStr = (String) message;
    byte[] msgBytes = msgStr.getBytes(charset.toString());
    byte[] lenBytes = Utils.intToByteArray(msgBytes.length);

    buffer.put(lenBytes);   // 将前4位设置成数据体的字节长度
    buffer.put(msgBytes);   // message content 
    
    buffer.flip();
    out.write(buffer);
  }

}