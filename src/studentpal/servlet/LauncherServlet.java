package studentpal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.*;
import javax.servlet.*;

import studentpal.engine.ServerEngine;
import studentpal.ws.WsService;

public class LauncherServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
     PrintWriter out = res.getWriter();
     out.println("Hello, world!");
     out.close();
  }

  @Override
  public void init() throws ServletException {
    super.init();
    
    ServerEngine.getInstance().initialize();
    // Sample: http://<Server's IP>:9090/StudentPal/PhoneConnector?wsdl
    WsService.getInstance().publishWs();
  }
  
  
}
