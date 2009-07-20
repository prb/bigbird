package bigbird.web;

import bigbird.voldemort.AbstractVoldemortTest;
import org.junit.Test;

public class MultiuserSmokeTest extends AbstractVoldemortTest {

    @Test
    public void testBasicTweet() throws Exception {
        queue.setIncrement(1000);

        Initializer init = new Initializer();
        init.setUserService(userService);
        init.setTweetService(tweetService);
        init.setCommandQueue(queue);
        init.setMaxUsers(1000);
        init.initializeUsers();
        init.initializeTweets();
    }

    @Override
    protected String getVoldemortHome() {
        return "../voldemort/" + super.getVoldemortHome();
    }

}
