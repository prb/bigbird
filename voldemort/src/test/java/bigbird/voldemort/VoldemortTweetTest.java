package bigbird.voldemort;

import bigbird.BackendException;
import bigbird.Tweet;
import bigbird.User;
import bigbird.UserNotFoundException;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class VoldemortTweetTest extends AbstractVoldemortTest {

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
        
        List<Tweet> timeline = tweetService.getFriendsTimeline("admin", 0, 100);
        assertEquals(1, timeline.size());
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

        doStartFollow();
        // test idempotentcy
        doStartFollow();

        doStopFollow();
        // test idempotentcy
        doStopFollow();
        
        tweet("admin", "Hello 2");
        tweet("paul", "Hello 3");
        tweet("admin", "Hello 4");
        tweet("paul", "Hello 5");
        
        tweetService.startFollowing("dan", "admin");

        Thread.sleep(500);
        
        List<Tweet> friendsTimeline = tweetService.getFriendsTimeline("dan", 0, 100);
        assertEquals(3, friendsTimeline.size());
        
        String tweetId = tweet("admin", "Hello 6");
        
        Thread.sleep(1000);
        
        friendsTimeline = tweetService.getFriendsTimeline("dan", 0, 100);
        assertEquals(4, friendsTimeline.size());
        
        assertEquals(tweetId, friendsTimeline.get(0).getId());
        
        tweetService.startFollowing("dan", "paul");

        Thread.sleep(1000);
        
        friendsTimeline = tweetService.getFriendsTimeline("dan", 0, 100);
        assertEquals(6, friendsTimeline.size());
    }

    private void doStopFollow() throws BackendException, InterruptedException, UserNotFoundException {
        Set<String> followers;
        Set<String> following;
        List<Tweet> friendsTimeline;
        tweetService.stopFollowing("dan", "admin");
        
        Thread.sleep(500);
        
        followers = tweetService.getFollowers("admin");
        assertNotNull(followers);
        assertFalse(followers.contains("dan"));
        
        following = tweetService.getFollowing("dan");
        assertNotNull(following);
        assertFalse(following.contains("admin"));
        
        friendsTimeline = tweetService.getFriendsTimeline("dan", 0, 100);
        assertEquals(0, friendsTimeline.size());
    }

    private void doStartFollow() throws BackendException, InterruptedException, UserNotFoundException {
        // Dan is now following Admin
        tweetService.startFollowing("dan", "admin");
        
        Thread.sleep(500);
        
        Set<String> followers = tweetService.getFollowers("admin");
        assertNotNull(followers);
        assertTrue(followers.contains("dan"));
        
        Set<String> following = tweetService.getFollowing("dan");
        assertNotNull(following);
        assertTrue(following.contains("admin"));
        
        List<Tweet> friendsTimeline = tweetService.getFriendsTimeline("dan", 0, 100);
        assertEquals(1, friendsTimeline.size());
    }

    private String tweet(String user, String message) throws UserNotFoundException, BackendException {
        Tweet tweet = new Tweet();
        tweet.setText(message);
        tweet.setDate(new Date());
        tweet.setUser(user);
        
        tweetService.tweet(tweet);
        
        return tweet.getId();
    }
}
