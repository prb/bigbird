package bigbird.voldemort;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public class AbstractVoldemortTest extends Assert {
    
    protected EmbeddedVoldemort voldemort;
    protected boolean stop = true;
    
    @Before
    public void setUp() throws Exception {
        voldemort = new EmbeddedVoldemort();
        voldemort.setVoldemortHome(getVoldemortHome());
        voldemort.start();
    }

    protected String getVoldemortHome() {
        return "./src/test/resources";
    }

    @After
    public void tearDown() throws Exception {
        if (voldemort != null && stop) {
            voldemort.stop();
        }
    }
}
