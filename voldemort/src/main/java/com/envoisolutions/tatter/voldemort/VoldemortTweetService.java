package com.envoisolutions.tatter.voldemort;

import bigbird.NotFoundException;
import bigbird.Tweet;
import bigbird.TweetService;
import bigbird.UserNotFoundException;
import bigbird.impl.AbstractMapStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import voldemort.client.StoreClient;
import voldemort.client.StoreClientFactory;
import voldemort.versioning.Versioned;

public class VoldemortTweetService extends AbstractMapStore implements TweetService {
    private StoreClient<String, Map<String,String>> users;
    private StoreClient<String, Map<String,String>> tweets;
    
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
            
            Map<String, String> tweetMap = tweets.getValue(userId + "-" + idx);
            Tweet tweet = toTweet(tweetMap);
            
            tweetList.add(tweet);
        }
        
        return tweetList;
    }

    public List<Tweet> getFollowingTweets(String user, int start, int count) throws UserNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    public void startFollowing(String user, String toStartUser) {
        // TODO Auto-generated method stub
        
    }

    public void stopFollowing(String user, String toStopUser) {
        // TODO Auto-generated method stub
        
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
        Tweet tweet = toTweet(tweetMap);
        
        return tweet;
    }


    public void tweet(Tweet tweet) throws UserNotFoundException {
        Map<String, String> tweetMap = toMap(tweet);
        
        String userId = tweet.getUser();
        Versioned<Map<String, String>> versioned = users.get(userId);
        Map<String, String> user = versioned.getValue();
        
        if (user == null) throw new UserNotFoundException(userId);
        
        String lastTweetStr = user.get(LAST_TWEET);
        int lastTweet = Integer.valueOf(lastTweetStr);
        int tweetIdx = ++lastTweet;
        user.put(LAST_TWEET, new Integer(tweetIdx).toString());
        
        String id = userId + "-" + tweetIdx;
        
        if (!tweets.putIfNotObsolete(id, new Versioned(tweetMap))) {
            
        };
        
        // Update last tweet..
        // TODO: compensate if this fails
        users.putIfNotObsolete(userId, versioned);
        tweet.setId(id);
    }
    
    public void delete(String id) throws NotFoundException {
        // TODO Auto-generated method stub
        
    }
    
    public void setStoreClientFactory(StoreClientFactory storeClientFactory) {
        tweets = storeClientFactory.getStoreClient("tweets");
        users = storeClientFactory.getStoreClient("users");
    }

}
