package bigbird.web;

import bigbird.BackendException;
import bigbird.Tweet;
import bigbird.TweetService;
import bigbird.User;
import bigbird.UserNotFoundException;
import bigbird.UserService;
import bigbird.queue.AbstractCommandQueue;
import bigbird.queue.CommandQueue;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Initializes BigBird with some default data.
 */
public class Initializer {

    private int maxUsers = 10000;
    private ThreadPoolExecutor executor;
    private int count = 0;
    
    private UserService userService;
    private TweetService tweetService;
    private int popularCutoff;
    private int engagedCutoff;
    private CommandQueue commandQueue;
    private int highlyEngagedCutoff;
    
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] { "classpath:applicationContext-web.xml" });
        
        Initializer i = new Initializer();
        i.setTweetService((TweetService) ctx.getBean("tweetService"));
        i.setUserService((UserService) ctx.getBean("userService"));
        i.setCommandQueue((CommandQueue) ctx.getBean("commandQueue"));
        i.initializeUsers();
        i.initializeTweets();
    }
    
    public void initializeUsers() throws Exception {
        User first = userService.getUser("user" + maxUsers);
        if (first != null) return;
        
        for (int i = maxUsers-1; i >= 0; i--) {
            User user = new User();
            user.setUsername("user" + i);
            user.setName("User " + i);
            userService.newUser(user, "user" + i);
            
            if ((maxUsers - i) % 100 == 0) {
                System.out.println("Created " + (maxUsers-i) + "/" + maxUsers + " new users.");
            }
        }
        
        System.out.println("Created " + maxUsers + " users");
        
        /**
         * Types of users:
         * 1. Popular: e.g. aston kutcher. Followed by tons of people, follows about 50.
         * 2. Engaged users: followers ~= follows. Can vary from 10 to 500. Will follow
         * a normal distribution over this % of followers.
         * 3. Zombies: follow just a couple people, followed by no one. (In our case we're
         * going to make zombies follow popular users.
         */
        
        double percentPopular = 0.0010;
        double percentHighlyEngaged = 0.1;
        double percentEngaged = 0.7;
        
        popularCutoff = (int) (percentPopular*(double)maxUsers);
        highlyEngagedCutoff = (int) (percentHighlyEngaged*(double)maxUsers);
        engagedCutoff = (int) (percentEngaged*(double)maxUsers);
        
        System.out.println("Initializing Followers. Popular cutoff: " + popularCutoff + ", Engaged cuttoff: " + engagedCutoff);
        int commandCounter = 0;
        for (int user = 0; user < maxUsers; user++) {
            String id = "user" + user;
            
            // Handle popular users.
            if (user < popularCutoff) {
                for (int j = 0; j < 50; j++) {
                    int follow = new Random().nextInt(maxUsers);
                    tweetService.startFollowing(id, "user" + follow);
                    commandCounter++;
                }
            }
            
            for (int j = 0; j < popularCutoff; j++) {
                tweetService.startFollowing(id, "user" + j);
                commandCounter++;
            }

            if (user > popularCutoff && user < highlyEngagedCutoff) {
                // Follow the next hundred users if they're highly engaged
                for (int j = 0; j < 100 && j < maxUsers; j++) {
                    tweetService.startFollowing(id, "user" + (j + popularCutoff));
                    commandCounter++;
                }
            }
            
            if (user > highlyEngagedCutoff && user < engagedCutoff) {
                // Follow the next 10 users if they're engaged
                for (int j = 0; j < 10 && j < maxUsers; j++) {
                    tweetService.startFollowing(id, "user" + (j + popularCutoff));
                    commandCounter++;
                }
            }
            
            if (user % 100 == 0) {
                System.out.println("Issued " + commandCounter + " follow commands (User " + user + "/" + maxUsers + ")");
            }
        }
        System.out.println("Issued " + commandCounter + " follow commands");
        waitForAllCommands();
    }

    private void waitForAllCommands() throws InterruptedException {
        System.out.println("Waiting for follow commands to finish.");
        Executor ex = ((AbstractCommandQueue)commandQueue).getCommandExecutor();
        BlockingQueue<Runnable> queue = null;
        if (ex instanceof ThreadPoolTaskExecutor) {
            queue = ((ThreadPoolTaskExecutor) ex).getThreadPoolExecutor().getQueue();
        } else if (ex instanceof ThreadPoolExecutor){
            queue = ((ThreadPoolExecutor) ex).getQueue();
        }
        
        while (queue.size() > 0) {
            Thread.sleep(1000);
            System.out.println("Queue depth: " + queue.size());
        }
        System.out.println("Finished follow commands!");
    }
    
    public void initializeTweets() throws Exception {
        System.out.println("Starting tweets");
        executor = new ThreadPoolExecutor(1, 100, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

        for (int id = 0; id < maxUsers; id++) {
            
            for (int i = 0; i < 5; i++) {
                final String userId = "user" + id;
                final String msg = "Tweet #" + i*id;
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
        
        while (count < maxUsers*5) {
            Thread.sleep(1000);
        }
    }

    private String tweet(String user, String message) throws UserNotFoundException, BackendException {
        Tweet tweet = new Tweet();
        tweet.setText(message);
        tweet.setDate(new Date());
        tweet.setUser(user);
        
        tweetService.tweet(tweet);
        
        return tweet.getId();
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setCommandQueue(CommandQueue commandQueue) {
        this.commandQueue = commandQueue;
    }
    
    public void setTweetService(TweetService tweetService) {
        this.tweetService = tweetService;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    
}
