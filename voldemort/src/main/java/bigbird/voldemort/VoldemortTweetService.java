package bigbird.voldemort;

import bigbird.BackendException;
import bigbird.NotFoundException;
import bigbird.Tweet;
import bigbird.TweetService;
import bigbird.UserNotFoundException;
import bigbird.impl.AbstractMapStore;
import bigbird.queue.CannotStoreCommandException;
import bigbird.queue.CommandQueue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import voldemort.client.StoreClient;
import voldemort.client.StoreClientFactory;
import voldemort.client.UpdateAction;
import voldemort.versioning.Versioned;

public class VoldemortTweetService extends AbstractMapStore implements TweetService {
    public static final String USERS_CLIENT = "usersClient";
    public static final String TWEETS_CLIENT = "tweetsClient";
    public static final String FRIENDS_TIMELINE_CLIENT = "friendsTimelineClient";
    public final static String SERVICE = "service";
    
    protected static final String FOLLOWING = "following";
    protected static final String FOLLOWERS = "followers";
    protected static final int MAX_TWEETS_IN_TIMELINE = 250;
    
    private StoreClient<String, Map<String,String>> users;
    private StoreClient<String, List<String>> friendsTimeline;
    private StoreClient<String, Map<String,String>> tweets;
    private CommandQueue commandQueue;
    private StoreClientFactory storeClientFactory;
    
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
        if (versioned == null) {
            versioned = new Versioned(new ArrayList<String>());
        }
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

    public void startFollowing(final String userId, final String toStartUser) throws BackendException {
        try {
            commandQueue.addAsync(new StartFollowingCommand(userId, toStartUser));
        } catch (CannotStoreCommandException e) {
            throw new BackendException(e);
        }
    }

    public void stopFollowing(String userId, String toStopUser) throws BackendException {
        try {
            commandQueue.addAsync(new StopFollowingCommand(userId, toStopUser));
        } catch (CannotStoreCommandException e) {
            throw new BackendException(e);
        }
    }
    
    protected static String toString(Set<String> followers) {
        StringBuilder sb = new StringBuilder();
        for (String s : followers) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    protected static Set<String> asSet(String string) {
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

    public Set<String> getFollowers(String userId) throws UserNotFoundException {
        Map<String, String> user = users.getValue(userId);
        if (user == null) throw new UserNotFoundException(userId);
        return asSet(user.get(FOLLOWERS));
    }

    public Set<String> getFollowing(String userId) throws UserNotFoundException {
        Map<String, String> user = users.getValue(userId);
        if (user == null) throw new UserNotFoundException(userId);
        return asSet(user.get(FOLLOWING));
    }


    public void tweet(final Tweet tweet) throws UserNotFoundException, BackendException {
        tweet.setDate(new Date());
        final Map<String, String> tweetMap = toMap(tweet);
        
        final String userId = tweet.getUser();
        
        Versioned<Map<String, String>> versioned = users.get(userId);
        if (versioned == null) throw new UserNotFoundException(userId);
        
        // Increment the last tweet counter and then create a command which will store the tweet
        // and update everyone's timelines.

        boolean success = users.applyUpdate(new UpdateAction<String, Map<String,String>>() {

            @Override
            public void update(StoreClient<String, Map<String, String>> arg0) {
                final Versioned<Map<String, String>> versioned = users.get(userId);
                final Map<String, String> user = versioned.getValue();
                
                String lastTweetStr = user.get(VoldemortTweetService.LAST_TWEET);
                int lastTweet = Integer.valueOf(lastTweetStr);
                int tweetIdx = ++lastTweet;
                user.put(VoldemortTweetService.LAST_TWEET, new Integer(tweetIdx).toString());
                
                // Increment the tweet counter. This action will be retried if it fails.
                users.put(userId, versioned);

                String tweetId = userId + "-" + tweetIdx;
                
                try {
                    commandQueue.add(new TweetCommand(userId, tweetId, tweetMap));
                } catch (CannotStoreCommandException e) {
                    throw new RuntimeException(e);
                }
                
                tweet.setId(tweetId);
            }
        });
        
        if (!success) {
            throw new BackendException("Could not store tweet. Please try again later.");
        }
    }
    
    public void delete(String id) throws NotFoundException {
        // TODO Auto-generated method stub
        
    }
    
    public void initialize() {
        tweets = storeClientFactory.getStoreClient("tweets");
        users = storeClientFactory.getStoreClient("users");
        friendsTimeline = storeClientFactory.getStoreClient("friendsTimeline");
        
        commandQueue.getCommandContext().put(SERVICE, this);
        commandQueue.getCommandContext().put(TWEETS_CLIENT, tweets);
        commandQueue.getCommandContext().put(USERS_CLIENT, users);
        commandQueue.getCommandContext().put(FRIENDS_TIMELINE_CLIENT, friendsTimeline);
    }
    
    public void setCommandQueue(CommandQueue commandQueue) {
        this.commandQueue = commandQueue;
    }

    public void setStoreClientFactory(StoreClientFactory storeClientFactory) {
        this.storeClientFactory = storeClientFactory;
    }

}
