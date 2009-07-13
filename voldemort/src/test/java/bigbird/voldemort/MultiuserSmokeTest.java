package bigbird.voldemort;

import org.junit.Test;

public class MultiuserSmokeTest extends AbstractVoldemortTest {

    @Test
    public void testBasicTweet() throws Exception {
        queue.setIncrement(1000);

        Initializer init = new Initializer();
        init.setUserService(userService);
        init.setTweetService(tweetService);
        init.initialize();
    }

}
