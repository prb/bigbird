package bigbird.web;


import bigbird.TweetService;
import bigbird.UserService;
import bigbird.queue.CommandQueue;

import java.util.HashMap;
import java.util.Map;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

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
        context.setDefaultsDescriptor("./src/test/resources/webdefault.xml");
        
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
        System.out.println("Starting.");
        EmbeddedWeb web = new EmbeddedWeb();
        web.start();
        
        System.out.println("Initializing.");
        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(web.getContext().getServletContext());
        Initializer i = new Initializer();
        i.setTweetService((TweetService) ctx.getBean("voldemortTweetService"));
        i.setUserService((UserService) ctx.getBean("voldemortUserService"));
        i.setCommandQueue((CommandQueue) ctx.getBean("commandQueue"));
//        i.initializeUsers();
//        i.initializeTweets();
    }

    public WebAppContext getContext() {
        return context;
    }

    
}
