package bigbird.web;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
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
        PostMethod tweetMethod = new PostMethod("http://localhost:8080/api/tweet");
        tweetMethod.setRequestEntity(new StringRequestEntity("{\"tweet\":\"Hello World\"}", "application/json", "UTF-8"));
        int result = client.executeMethod(tweetMethod);
        assertEquals(200, result);
        
        GetMethod get = new GetMethod("http://localhost:8080/api/users/admin?start=0&count=20");
        result = client.executeMethod(get);
        assertEquals(200, result);
        Thread.sleep(100000);
    }
}
