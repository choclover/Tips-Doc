package studentpal.model.codec;

import java.nio.charset.Charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class AsCodecFactory implements ProtocolCodecFactory {
  private ProtocolEncoder encoder;
  private ProtocolDecoder decoder;

  public AsCodecFactory() {
    encoder = new ResponseMsgEncoder(Charset.forName(CHARSET_NAME));;
    decoder = new ResponseMsgDecoder(Charset.forName(CHARSET_NAME));
  }

  public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
    return encoder;
  }

  public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
    return decoder;
  }
  
  public static final String CHARSET_NAME = "UTF-8";
  public static final int MSG_LENGTH_HEADER_SIZE = 4;
  
}
