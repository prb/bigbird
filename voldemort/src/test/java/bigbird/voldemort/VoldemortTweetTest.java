package bigbird.voldemort;

import bigbird.Tweet;
import bigbird.User;
import bigbird.UserNotFoundException;
import bigbird.voldemort.VoldemortTweetService;
import bigbird.voldemort.VoldemortUserService;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import voldemort.client.ClientConfig;
import voldemort.client.SocketStoreClientFactory;

public class VoldemortTweetTest extends AbstractVoldemortTest {
    private VoldemortTweetService tweetService;
    private VoldemortUserService userService;

    @Before
    public void setUpServices() throws Exception {
        ClientConfig config = new ClientConfig();
        config.setBootstrapUrls("tcp://localhost:6666");
        
        SocketStoreClientFactory factory = new SocketStoreClientFactory(config);
        tweetService = new VoldemortTweetService();
        tweetService.setStoreClientFactory(factory);
        
        userService = new VoldemortUserService();
        userService.setStoreClientFactory(factory);
        userService.initialize();
    }
    
    @Test
    public void testBasicTweet() throws Exception {
        Tweet tweet = new Tweet();
        tweet.setText("Hello World!");
        tweet.setDate(new Date());
        tweet.setUser("admin");
        
        tweetService.tweet(tweet);
        
        Tweet tweet2 = tweetService.getTweet(tweet.getId());
        assertNotNull(tweet2);
        assertEquals("admin-1", tweet.getId());
        assertEquals(tweet.getText(), tweet2.getText());
        
        List<Tweet> recent = tweetService.getRecentTweets(tweet.getUser());
        assertSame(1, recent.size());
    }
    
    @Test
    public void testFollow() throws Exception {
        User user = new User();
        user.setUsername("dan");
        user.setName("Dan Diephouse");
        userService.newUser(user, "dan");
        
        user = new User();
        user.setUsername("paul");
        user.setName("Paul R. Brown");
        userService.newUser(user, "paul");
        
        tweet("admin", "Hello World");

        // Dan is now following Admin
        tweetService.startFollowing("dan", "admin");
        
        List<Tweet> friendsTimeline = tweetService.getFriendsTimeline("dan", 0, 100);
        assertEquals(1, friendsTimeline.size());

        tweetService.stopFollowing("dan", "admin");
        
        friendsTimeline = tweetService.getFriendsTimeline("dan", 0, 100);
        assertEquals(0, friendsTimeline.size());
        
        tweet("admin", "Hello 2");
        tweet("paul", "Hello 3");
        tweet("admin", "Hello 4");
        tweet("paul", "Hello 5");
        
        tweetService.startFollowing("dan", "admin");
        
        friendsTimeline = tweetService.getFriendsTimeline("dan", 0, 100);
        assertEquals(3, friendsTimeline.size());
        
        tweetService.startFollowing("dan", "paul");
        
        friendsTimeline = tweetService.getFriendsTimeline("dan", 0, 100);
        assertEquals(5, friendsTimeline.size());
        
    }

    private void tweet(String user, String message) throws UserNotFoundException {
        Tweet tweet = new Tweet();
        tweet.setText(message);
        tweet.setDate(new Date());
        tweet.setUser(user);
        
        tweetService.tweet(tweet);
    }
}
