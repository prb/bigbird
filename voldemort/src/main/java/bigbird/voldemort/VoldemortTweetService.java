package bigbird.voldemort;

import bigbird.NotFoundException;
import bigbird.Tweet;
import bigbird.TweetService;
import bigbird.UserNotFoundException;
import bigbird.impl.AbstractMapStore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import voldemort.client.StoreClient;
import voldemort.client.StoreClientFactory;
import voldemort.client.UpdateAction;
import voldemort.versioning.Versioned;

public class VoldemortTweetService extends AbstractMapStore implements TweetService {
    protected static final String FOLLOWING = "following";
    private StoreClient<String, Map<String,String>> users;
    private StoreClient<String, List<String>> friendsTimeline;
    private StoreClient<String, Map<String,String>> tweets;
    private int MAX_TWEETS_IN_TIMELINE = 250;
    
    public List<Tweet> getRecentTweets(String user) throws UserNotFoundException {
        return getTweets(user, 0, 20);
    }

    public List<Tweet> getTweets(String userId, int start, int count) throws UserNotFoundException {
        Map<String, String> user = users.getValue(userId);
        if (user == null) throw new UserNotFoundException(userId);
        
        int lastTweet = Integer.valueOf(user.get(LAST_TWEET));
        
        List<Tweet> tweetList = new ArrayList<Tweet>();
        for (int i = start; i < start+count; i++) {
            int idx = lastTweet -i;

            if (idx == 0) break;
            
            String id = userId + "-" + idx;
            Map<String, String> tweetMap = tweets.getValue(id);
            Tweet tweet = toTweet(tweetMap, id);            
            tweetList.add(tweet);
        }
        
        return tweetList;
    }

    public List<Tweet> getFriendsTimeline(String userId, int start, int count) throws UserNotFoundException {
        Versioned<List<String>> versioned = friendsTimeline.get(userId);
        List<String> timeline = versioned.getValue();
        
        List<Tweet> tweetList = new ArrayList<Tweet>();
        
        // TODO: do asynchronously 
        
        for (int i = start; i < count && i < timeline.size(); i++) {
            String idAndTime = timeline.get(i);
            String[] split = idAndTime.split(":");
            
            String id = split[0];
            tweetList.add(toTweet(tweets.getValue(id), id));
        }
        return tweetList;
    }

    public void startFollowing(final String userId, final String toStartUser) {
        // 1. Update the list of followers for the user
        boolean success = users.applyUpdate(new UpdateAction<String,Map<String,String>>() {
            @Override
            public void update(StoreClient<String, Map<String, String>> client) {
                Versioned<Map<String, String>> versioned = client.get(userId);
                Map<String, String> user = versioned.getValue();
                Set<String> followers = asSet(user.get(FOLLOWING));
                
                followers.add(toStartUser);
                user.put(FOLLOWING, VoldemortTweetService.this.toString(followers));
                
                // Update the follower information
                client.put(userId, versioned);
            }
        });
        
        // 2. If that was successful, recompute the timeline
        if (success) {
            updateTimeline(userId, toStartUser);
        } else {
            // Rollback the addition of a follower
        }
    }

    /**
     * Add a user into a specific user's timeline.
     * @param userId
     * @param toStartUser
     */
    private void updateTimeline(String userId, String toStartUser) {
        Versioned<List<String>> versioned = friendsTimeline.get(userId);
        List<String> timeline;
        if (versioned == null) {
            timeline = new ArrayList<String>();
            versioned = new Versioned<List<String>>(timeline);
        } else {
            timeline = versioned.getValue();
        }
        
        try {
            List<Tweet> recentTweets = getRecentTweets(toStartUser);

            int timelineIdx = 0;
            long timelineDate = 0;

            for (Tweet tweet : recentTweets) {
                long date = tweet.getDate().getTime();
                
                while (timelineIdx < timeline.size() && timelineIdx < MAX_TWEETS_IN_TIMELINE) {
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
                
                if (timelineIdx == MAX_TWEETS_IN_TIMELINE) {
                    break;
                }
            }
            
            // TODO: handle failure
            friendsTimeline.put(userId, versioned);
        } catch (UserNotFoundException e) {
            // This shouldn't really happen.
            throw new RuntimeException(e);
        }
    }

    private void addToTimeline(List<String> timeline, int timelineIdx, Tweet tweet) {
        timeline.add(timelineIdx, tweet.getId() + ":" + tweet.getDate().getTime());
    }

    public void stopFollowing(String userId, String toStopUser) {
        Versioned<List<String>> versioned = friendsTimeline.get(userId);
        List<String> timeline;
        if (versioned == null) {
            return;
        } else {
            timeline = versioned.getValue();
        }
        
//        try {
            for (Iterator itr = timeline.iterator(); itr.hasNext();) {
                String idAndTime = (String)itr.next();
                
                String[] split = idAndTime.split(":");
                String id = split[0];
                
                if (id.startsWith(toStopUser + "-")) {
                    itr.remove();
                }
            }
            
            // TODO: add in more tweets from followers
            
            // TODO: handle failure
            friendsTimeline.put(userId, versioned);
//        } catch (UserNotFoundException e) {
//            // This shouldn't really happen.
//            throw new RuntimeException(e);
//        }
    }
    
    protected String toString(Set<String> followers) {
        StringBuilder sb = new StringBuilder();
        for (String s : followers) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    protected Set<String> asSet(String string) {
        if (string == null) {
            return new HashSet<String>();
        }
        
        String[] split = string.split(",");
        Set<String> set = new HashSet<String>();
        for (String s : split) {
            set.add(s);
        }
        return set;
    }

    public List<Tweet> search(String text, int start, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    public Tweet getTweet(String id) throws NotFoundException {
        Versioned<Map<String, String>> vTweet = tweets.get(id);
        if (vTweet == null) {
            throw new NotFoundException(id);
        }
        
        Map<String, String> tweetMap = vTweet.getValue();
        
        return toTweet(tweetMap, id);
    }


    public void tweet(final Tweet tweet) throws UserNotFoundException {
        tweet.setDate(new Date());
        final Map<String, String> tweetMap = toMap(tweet);
        
        final String userId = tweet.getUser();
        
        Versioned<Map<String, String>> versioned = users.get(userId);
        if (versioned == null) throw new UserNotFoundException(userId);
        
        users.applyUpdate(new UpdateAction<String, Map<String,String>>() {
            @Override
            public void update(StoreClient<String, Map<String, String>> arg0) {
                final Versioned<Map<String, String>> versioned = users.get(userId);
                final Map<String, String> user = versioned.getValue();
                
                String lastTweetStr = user.get(LAST_TWEET);
                int lastTweet = Integer.valueOf(lastTweetStr);
                int tweetIdx = ++lastTweet;
                user.put(LAST_TWEET, new Integer(tweetIdx).toString());
                
                // Increment the tweet counter. This action will be retried if it fails.
                users.put(userId, versioned);

                final String id = userId + "-" + tweetIdx;
                
                // Now actually insert the tweet
                tweets.applyUpdate(new UpdateAction<String, Map<String,String>>() {
                    @Override
                    public void update(StoreClient<String, Map<String, String>> client) {
                        tweets.put(id, new Versioned(tweetMap));
                    }
                });
                
                tweet.setId(id);
            }
        });
        
        // Update last tweet..
        // TODO: compensate if this fails
    }
    
    public void delete(String id) throws NotFoundException {
        // TODO Auto-generated method stub
        
    }
    
    public void setStoreClientFactory(StoreClientFactory storeClientFactory) {
        tweets = storeClientFactory.getStoreClient("tweets");
        users = storeClientFactory.getStoreClient("users");
        friendsTimeline = storeClientFactory.getStoreClient("friendsTimeline");
    }

}
