package bigbird.web;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import junit.framework.Assert;

public class WebTest extends Assert {
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
        HttpClient client = getHttpClient();
        
        PostMethod tweetMethod = new PostMethod("http://localhost:8080/api/tweet");
        tweetMethod.setRequestEntity(new StringRequestEntity("{\"tweet\":\"Hello World\"}", "application/json", "UTF-8"));
        int result = client.executeMethod(tweetMethod);
        assertEquals(200, result);
        
        GetMethod get = new GetMethod("http://localhost:8080/api/users/admin?start=0&count=20");
        result = client.executeMethod(get);
        assertEquals(200, result);
        System.out.println(get.getResponseBodyAsString());
        
        get = new GetMethod("http://localhost:8080/api/friendsTimeline?start=0&count=20");
        result = client.executeMethod(get);
        assertEquals(200, result);
        System.out.println(get.getResponseBodyAsString());
        
    }
    
    @Test
    public void testStats() throws Exception {
        HttpClient client = getHttpClient();

        // init the webapp
        GetMethod get = new GetMethod("http://localhost:8080/api/friendsTimeline?start=0&count=20");
        int result = client.executeMethod(get);
        assertEquals(200, result);
        
        // check stats
        get = new GetMethod("http://localhost:8080/stats");
        result = client.executeMethod(get);
        assertEquals(200, result);
        System.out.println(get.getResponseBodyAsString());
        
    }

    private HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);
        Credentials creds = new UsernamePasswordCredentials("admin", "password");
        client.getState().setCredentials(new AuthScope("localhost", 8080, AuthScope.ANY_REALM), creds);
        return client;
    }

    protected void login(final String username, final String password) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username, password));
    }

    protected WebApplicationContext getApplicationContext() {
        return WebApplicationContextUtils.getWebApplicationContext(jetty.getContext().getServletContext());
    }
}

