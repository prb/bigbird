package bigbird.voldemort;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import voldemort.server.VoldemortConfig;
import voldemort.server.VoldemortServer;

public class AbstractVoldemortTest extends Assert {
    
    private VoldemortServer server;

    @Before
    public void setUp() throws Exception {
        VoldemortConfig config = VoldemortConfig.loadFromVoldemortHome(getVoldemortHome());
        server = new VoldemortServer(config);
        server.start();
    }

    protected String getVoldemortHome() {
        return "./src/test/resources";
    }

    @After
    public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }
}
