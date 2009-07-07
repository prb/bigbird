package bigbird.voldemort;

import voldemort.server.AbstractService;
import voldemort.server.VoldemortConfig;
import voldemort.server.VoldemortServer;

public class EmbeddedVoldemort {
    private String voldemortHome;
    private AbstractService server;
    
    public void start() {
        VoldemortConfig config = VoldemortConfig.loadFromVoldemortHome(getVoldemortHome());
        server = new VoldemortServer(config);
        server.start();
    }
    
    public void stop() {
        server.stop();
    }

    public String getVoldemortHome() {
        return voldemortHome;
    }

    public void setVoldemortHome(String voldemortHome) {
        this.voldemortHome = voldemortHome;
    }
    
}
