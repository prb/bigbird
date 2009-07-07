package bigbird.couchdb;

import bigbird.NotFoundException;
import bigbird.Tweet;
import bigbird.TweetService;
import bigbird.UserNotFoundException;
import bigbird.impl.AbstractMapStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Options;
import org.jcouchdb.document.DesignDocument;
import org.jcouchdb.document.ValueRow;
import org.jcouchdb.document.View;
import org.jcouchdb.document.ViewResult;
import org.jcouchdb.exception.UpdateConflictException;
import org.svenson.JSONParser;

public class CouchDbTweetService extends AbstractMapStore implements TweetService {

    private static final String TYPE = "type";

    private static final String ID = "_id";
    
    private Database db;
    
    public void initialize() throws Exception {
        // Create views...
        DesignDocument dd = new DesignDocument("tweets");
        // All tweets
        dd.addView("all", new View("function (doc) { if (doc.type == 'tweet') { emit(null, doc); } }"));
        // Tweets sorted by user
        dd.addView("by_user", new View("function (doc) { if (doc.type == 'tweet') { emit(doc.user, doc); } }"));
        try {
            db.createOrUpdateDocument(dd);
        } catch (UpdateConflictException e) {
            
        }
    }

    public List<Tweet> getRecentTweets(String user) throws UserNotFoundException {
        return getTweets(user, 0, 20);
    }

    public Tweet getTweet(String id) throws NotFoundException {
        Map<String, String> doc = db.getDocument(Map.class, id);
        
        return toTweet(doc, id);
    }

    public List<Tweet> getTweets(String user, int start, int count) throws UserNotFoundException {
        Options opts = new Options().limit(count).descending(true).key(user).skip(start);
        
        ViewResult<Map> result = db.queryView("tweets/by_user", 
                                              Map.class, 
                                              opts, 
                                              new JSONParser());
        
        List<Tweet> tweets = new ArrayList<Tweet>();
        for (ValueRow<Map> row : result.getRows()) {
            tweets.add(toTweet(row.getValue(), (String)row.getKey()));
        }
        
        return tweets;
    }
    public List<Tweet> search(String text, int start, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    public void delete(String id) throws NotFoundException {
        // TODO Auto-generated method stub
        
    }

    public List<Tweet> getFriendsTimeline(String user, int start, int count) throws UserNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    public void startFollowing(String user, String toStartUser) {
        // TODO Auto-generated method stub
        
    }

    public void stopFollowing(String user, String toStopUser) {
        // TODO Auto-generated method stub
        
    }

    public void tweet(Tweet tweet) throws UserNotFoundException {
        Map<String, String> tweetMap = toMap(tweet);

        tweetMap.put(TYPE, "tweet");

        // TODO: deal with conflict
        db.createDocument(tweetMap);

        tweet.setId(tweetMap.get(ID));
    }

    public void setDatabase(Database db) {
        this.db = db;
    }

}