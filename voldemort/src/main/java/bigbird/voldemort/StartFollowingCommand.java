package bigbird.voldemort;


import bigbird.Tweet;
import bigbird.UserNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import voldemort.client.StoreClient;
import voldemort.versioning.Versioned;

/**
 * 1. Update the list of followers for the user
 * 2. Update the list of people following the newly followed user
 * 3. Recompute the user's timeline
 */
public class StartFollowingCommand extends AbstractVoldemortCommand {

    private String userId;
    private String toStartUser;
    
    public StartFollowingCommand(String userId, String follow) {
        super();
        this.userId = userId;
        this.toStartUser = follow;
    }

    @Override
    protected Object execute(VoldemortTweetService service,
                             StoreClient<String, List<String>> friendsTimeline,
                             StoreClient<String, Map<String, String>> users, 
                             StoreClient<String, Map<String, String>> tweets) {
        // 1. Update the list of followers for the user
        updateFollowing(users);
        
        // 2. Update the FOLLOWERS of toStartUser
        updateFollowers(users);
        
        
        // 3. Update the timeline of the user
        Versioned<List<String>> versionedTimeline = friendsTimeline.get(userId);
        List<String> timeline;
        if (versionedTimeline == null) {
            timeline = new ArrayList<String>();
            versionedTimeline = new Versioned<List<String>>(timeline);
        } else {
            timeline = versionedTimeline.getValue();
        }
        
        try {
            List<Tweet> recentTweets = service.getRecentTweets(toStartUser);

            int timelineIdx = 0;
            long timelineDate = 0;

            for (Tweet tweet : recentTweets) {
                long date = tweet.getDate().getTime();
                
                while (timelineIdx < timeline.size() && timelineIdx < VoldemortTweetService.MAX_TWEETS_IN_TIMELINE) {
                    String id = timeline.get(timelineIdx);
                    String[] split = id.split(":");
                    timelineDate = new Long(split[1]);
                    
                    if (timelineDate > date) {
                        addToTimeline(timeline, timelineIdx, tweet);
                        timelineIdx++;
                        break;
                    }
                    
                    timelineIdx++;
                }
                
                if (timelineIdx == timeline.size()) {
                    addToTimeline(timeline, timelineIdx, tweet);
                }
                
                if (timelineIdx == VoldemortTweetService.MAX_TWEETS_IN_TIMELINE) {
                    break;
                }
            }
            
            // TODO: handle failure
            friendsTimeline.put(userId, versionedTimeline);
        } catch (UserNotFoundException e) {
            // I guess the user deleted their account... Stupid users.
        }
        
        return null;
    }

    private void updateFollowing(StoreClient<String, Map<String, String>> users) {
        Versioned<Map<String, String>> versionedUser = users.get(userId);
        Map<String, String> user = versionedUser.getValue();
        Set<String> followers = VoldemortTweetService.asSet(user.get(VoldemortTweetService.FOLLOWING));
        
        followers.add(toStartUser);
        user.put(VoldemortTweetService.FOLLOWING, VoldemortTweetService.toString(followers));
        
        // Update the follower information
        users.put(userId, versionedUser);
    }

    private void updateFollowers(StoreClient<String, Map<String, String>> users) {
        Versioned<Map<String, String>> versionedUser = users.get(toStartUser);
        Map<String, String> user = versionedUser.getValue();
        Set<String> followers = VoldemortTweetService.asSet(user.get(VoldemortTweetService.FOLLOWERS));
        
        followers.add(userId);
        user.put(VoldemortTweetService.FOLLOWERS, VoldemortTweetService.toString(followers));
        
        // Update the follower information
        users.put(toStartUser, versionedUser);
    }
    
    private void addToTimeline(List<String> timeline, int timelineIdx, Tweet tweet) {
        timeline.add(timelineIdx, tweet.getId() + ":" + tweet.getDate().getTime());
    }
}