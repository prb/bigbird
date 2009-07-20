package bigbird.voldemort;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import voldemort.client.StoreClient;
import voldemort.versioning.Versioned;

/**
 * 1. Insert the tweet into the tweets store
 * 2. Update follower's timelines
 */
public class TweetCommand extends AbstractVoldemortCommand {
    protected final Log log = LogFactory.getLog(getClass());
    
    private String userId;
    private final Map<String, String> tweetMap;
    transient String tweetId;

    public TweetCommand(String userId, String tweetId, Map<String, String> tweetMap) {
        this.userId = userId;
        this.tweetId = tweetId;
        this.tweetMap = tweetMap;
    }   

    public Object execute(VoldemortTweetService service,
                          final StoreClient<String,List<String>> friendsTimeline, 
                          StoreClient<String,Map<String,String>> users,
                          StoreClient<String,Map<String, String>> tweets) {
        // Now actually insert the tweet
        tweets.put(tweetId, new Versioned(tweetMap));
        
        // Update follower's timelines
        Map<String, String> user = users.getValue(userId);
        if (user == null) {
            log.error("Could not tweet as user " + userId + " was missing");
            return null;
        }
        
        final Set<String> followers = VoldemortTweetService.asSet(user.get(VoldemortTweetService.FOLLOWERS));
        final CountDownLatch latch = new CountDownLatch(followers.size());
        
        int i = 0;
        for (final String follower : followers) {
            Runnable timelineUpdater = new Runnable() {
                public void run() {
                    updateTimeline(friendsTimeline, follower);
                    latch.countDown();
                }
            };
            service.getExecutor().execute(timelineUpdater);
            i++;
        }
        
        // Insert the tweet into the user's timeline as well
        updateTimeline(friendsTimeline, userId);
        
        try {
            latch.await(service.getAsyncTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        
        return null;
    }

    private void updateTimeline(StoreClient<String, List<String>> friendsTimeline, 
                                String user) {
        Versioned<List<String>> timeline = friendsTimeline.get(user);
         if (timeline == null) {
            timeline = new Versioned<List<String>>(new ArrayList<String>());
        }
        insertTweet(timeline);
        
        friendsTimeline.put(user, timeline);
    }

    private void insertTweet(Versioned<List<String>> timeline) {
        String dateStr = tweetMap.get(VoldemortTweetService.DATE);
        Long date = new Long(dateStr);
        
        String timelineId = tweetId + ":" + dateStr;
        
        List<String> timelineList = timeline.getValue();
        for (int i = 0; i < timelineList.size(); i++) {
            String id = timelineList.get(0);
            String[] split = id.split(":");
            Long timelineDate = new Long(split[1]);
            
            if (timelineDate < date) {
                timelineList.add(i, timelineId);
                return;
            }
        }
        
        timelineList.add(timelineId);
    }

}
