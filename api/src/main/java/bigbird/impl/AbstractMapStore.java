package bigbird.impl;

import bigbird.Tweet;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AbstractMapStore {
    // Tweet constants
    public static final String DATE = "date";
    public static final String TEXT = "text";
    public static final String USER = "user";
    
    // User constants
    public static final String LAST_TWEET = "lastTweet";

    protected Map<String, String> toMap(Tweet tweet) {
        Map<String, String> tweetMap = new HashMap<String,String>();
        tweetMap.put(DATE, new Long(tweet.getDate().getTime()).toString());
        tweetMap.put(TEXT, tweet.getText());
        tweetMap.put(USER, tweet.getUser());
        return tweetMap;
    }
    
    protected Tweet toTweet(Map<String, String> tweetMap) {
        Tweet tweet = new Tweet();
        tweet.setDate(new Date(Long.valueOf(tweetMap.get(DATE))));
        tweet.setText(tweetMap.get(TEXT));
        tweet.setUser(tweetMap.get(USER));
        return tweet;
    }
}
