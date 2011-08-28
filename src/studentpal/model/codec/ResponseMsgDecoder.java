package studentpal.model.codec;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import studentpal.util.Utils;

public class ResponseMsgDecoder extends CumulativeProtocolDecoder {
  private static final Logger logger = LoggerFactory.getLogger(ResponseMsgDecoder.class);
  private final Charset charset;

  public ResponseMsgDecoder(Charset charset) {
    this.charset = charset;
  }

  /**
   * 这个方法的返回值是重点： 
   * 1、当内容刚好时，返回false，告知父类接收下一批内容
   *  
   * 2、内容不够时需要下一批发过来的内容，此时返回false，这样父类
   * CumulativeProtocolDecoder 会将内容放进IoSession中，等下次来数据后就自动拼装再交给本类的doDecode
   * 
   * 3、当内容多时，返回true，因为需要再将本批数据进行读取，父类会将剩余的数据再次推送本类的doDecode
   */
  public boolean doDecode(IoSession session, IoBuffer in,
      ProtocolDecoderOutput out) throws Exception {

    if (in.remaining() > 0) { // 有数据时，读取4字节判断消息长度
      byte[] lenBytes = new byte[AsCodecFactory.MSG_LENGTH_HEADER_SIZE];
      in.mark();          // 标记当前位置，以便后面reset
      in.get(lenBytes);  // 读取前4字节
      
      // NumberUtil是自己写的一个int转byte[]的一个工具类
      int msgLen = Utils.byteArrayToInt(lenBytes);
      
      // 如果消息内容的长度不够则直接返回true
      if (msgLen > in.remaining()) {// 如果消息内容不够，则重置，相当于不读取size
        in.reset();
        return false;// 继续接收新数据，以拼凑成完整数据
        
      } else {
        byte[] msgBytes = new byte[msgLen];
        in.get(msgBytes, 0, msgLen);
        String msgStr = new String(msgBytes, charset.toString());
        
        if (null != msgStr && msgStr.length() > 0) {
          logger.debug("Got Message is:\n", msgStr);
          out.write(msgStr);
        } else {
          logger.debug("Got Message is NULL or empty!\n");
        }
        
        if (in.remaining() > 0) {// 如果读取内容后还粘了包，就让父类再给俺 一次，进行下一次解析
          return true;
        }
      }
    }
    
    return false;// 处理成功，让父类进行接收下个包
  }

  
}
