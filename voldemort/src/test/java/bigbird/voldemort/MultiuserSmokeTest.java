package bigbird.voldemort;

import bigbird.BackendException;
import bigbird.Tweet;
import bigbird.User;
import bigbird.UserNotFoundException;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class MultiuserSmokeTest extends AbstractVoldemortTest {

    private static final int MAX_USERS = 1000;
    private ThreadPoolExecutor executor;

    private int count = 0;
    @Test
    public void testBasicTweet() throws Exception {
        queue.setIncrement(1000);
        
        for (int i = 0; i < MAX_USERS; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setName("User " + i);
            userService.newUser(user, "user" + i);
        }
        
        executor = new ThreadPoolExecutor(1, 100, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
     
        System.out.println("Initializing Followers");
        for (int i = 0; i < MAX_USERS; i++) {
            String id = "user" + i;
            
            if (i < 900) {
                for (int j = 0; j < 5; j++) {
                    int follow = new Random().nextInt(1000);
                    tweetService.startFollowing(id, "user" + follow);
                }
            } else if (i < 990) {
                for (int j = 0; j < 100; j++) {
                    int follow = new Random().nextInt(1000);
                    tweetService.startFollowing(id, "user" + follow);
                }
            }  else  {
                for (int j = 0; j < 1000; j++) {
                    tweetService.startFollowing(id, "user" + j);
                }
            } 
        }
        
        System.out.println("Starting tweets");
        for (int id = 0; id < 1000; id++) {
            for (int i = 0; i < 5; i++) {
                final String userId = "user" + id;
                final String msg = "Tweet #" + i;
                executor.execute(new Runnable() {
                    public void run() {
                        try {
                            tweet(userId, msg);
                            count++;
                            
                            if (count % 1000 == 0) {
                                System.out.println("Tweets: " + count);
                            }
                        } catch (UserNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (BackendException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        
        while (count < 5000) {
            Thread.sleep(1000);
        }
        
        executor.shutdown();
        queue.shutdown();
        
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
