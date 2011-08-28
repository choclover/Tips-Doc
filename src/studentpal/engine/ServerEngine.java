package studentpal.engine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import studentpal.model.codec.AsCodecFactory;
import studentpal.model.codec.ResponseMsgDecoder;
import studentpal.model.codec.ResponseMsgEncoder;
import studentpal.ws.WsService;

public class ServerEngine {
  public static final int SVR_PORT = 9177;
  public static final int SVR_PORT_BACKUP = 9123;
  
  private static final int BUFFER_SIZE = 8192;

  private static ServerEngine engineInst = null;
  private static MessageHandler msgHandler = null;
  
  public static ServerEngine getInstance() {
    if (engineInst == null) {
      engineInst = new ServerEngine();
      msgHandler = new MessageHandler(engineInst);
    }
    return engineInst;
  }

  public MessageHandler getMsgHandler() {
    return msgHandler;
  }
  
  public void initialize() {
    try {
      IoAcceptor acceptor = new NioSocketAcceptor();
      acceptor.getSessionConfig().setReadBufferSize(BUFFER_SIZE);
      
      DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
      //chain.addLast("logger", new LoggingFilter());
      chain.addLast("codec", new ProtocolCodecFilter(
//          new ResponseMsgEncoder(Charset.forName(AsCodecFactory.CHARSET_NAME)), 
//          new ResponseMsgDecoder(Charset.forName(AsCodecFactory.CHARSET_NAME))
          new AsCodecFactory()
      ));
  
      acceptor.setHandler(new PhoneConnctor());
      acceptor.setDefaultLocalAddress(new InetSocketAddress(SVR_PORT));
      acceptor.bind();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    ServerEngine.getInstance().initialize();

    /*
     * 启动WS
     */
    WsService.getInstance().publishWs();
  }
}
