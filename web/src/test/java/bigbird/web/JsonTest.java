package bigbird.web;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class JsonTest extends Assert {
    private EmbeddedWeb jetty;

    @Before
    public void setupJetty() throws Exception {
        jetty = new EmbeddedWeb();
        jetty.start();
    }
    
    @After
    public void stopJetty() throws Exception {
        jetty.stop();
    }
    
    @Test
    public void testGetUser() throws Exception {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod("http://localhost:8080/admin?start=0&count=20");
        int result = client.executeMethod(get);
        assertEquals(200, result);
        Thread.sleep(100000);
    }
}
