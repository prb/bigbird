package com.envoisolutions.tatter.voldemort;

import bigbird.Tweet;
import com.envoisolutions.tatter.voldemort.VoldemortTweetService;
import com.envoisolutions.tatter.voldemort.VoldemortUserService;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import voldemort.client.SocketStoreClientFactory;

public class TatterTest extends AbstractVoldemortTest {
    @Test
    public void testFoo() throws Exception {
        SocketStoreClientFactory factory = new SocketStoreClientFactory("tcp://localhost:6666");
        VoldemortTweetService vts = new VoldemortTweetService();
        vts.setStoreClientFactory(factory);
        
        VoldemortUserService vus = new VoldemortUserService();
        vus.setStoreClientFactory(factory);
        vus.initialize();
        
        Tweet tweet = new Tweet();
        tweet.setText("Hello World!");
        tweet.setDate(new Date());
        tweet.setUser("admin");
        
        vts.tweet(tweet);
        
        Tweet tweet2 = vts.getTweet(tweet.getId());
        assertNotNull(tweet2);
        assertEquals("admin-1", tweet.getId());
        assertEquals(tweet.getText(), tweet2.getText());
        
        List<Tweet> recent = vts.getRecentTweets(tweet.getUser());
        assertSame(1, recent.size());
    }
}
