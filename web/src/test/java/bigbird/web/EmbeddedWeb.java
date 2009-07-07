package bigbird.web;


import java.util.HashMap;
import java.util.Map;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

public class EmbeddedWeb {

    private Server server;
    
    public void start() throws Exception {
        server = new Server();
        
        Connector connector = new SelectChannelConnector();
        connector.setPort(8080);
        connector.setHost("0.0.0.0");
        server.addConnector(connector);

        
        WebAppContext wac = new WebAppContext();
        wac.setContextPath("/");
        wac.setWar("./src/main/webapp");
        
        Map<String, Boolean> params = new HashMap<String, Boolean>();
        params.put("useFileMappedBuffer", Boolean.FALSE);
        wac.setInitParams(params);
        
        server.addHandler(wac);
        
        server.setStopAtShutdown(true);

        server.start();
    }
    
    public void stop() throws Exception {
        server.stop();
    }
    
    public static void main(String[] args) throws Exception {
        new EmbeddedWeb().start();
    }

}
