package bigbird.web;


import java.util.HashMap;
import java.util.Map;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

public class EmbeddedWeb {

    private Server server;
    private WebAppContext context;
    
    public void start() throws Exception {
        server = new Server();
        
        Connector connector = new SelectChannelConnector();
        connector.setPort(8080);
        connector.setHost("0.0.0.0");
        server.addConnector(connector);

        
        context = new WebAppContext();
        context.setContextPath("/");
        context.setWar("./src/main/webapp");
        
        Map<String, Boolean> params = new HashMap<String, Boolean>();
        params.put("useFileMappedBuffer", Boolean.FALSE);
        context.setInitParams(params);
        
        server.addHandler(context);
        
        server.setStopAtShutdown(true);

        server.start();
    }
    
    public void stop() throws Exception {
        server.stop();
    }
    
    public static void main(String[] args) throws Exception {
        new EmbeddedWeb().start();
    }

    public WebAppContext getContext() {
        return context;
    }

    
}
