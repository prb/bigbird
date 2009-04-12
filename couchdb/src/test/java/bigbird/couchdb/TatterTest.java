package bigbird.couchdb;

import bigbird.Tweet;
import bigbird.couchdb.CouchDbTweetService;
import bigbird.couchdb.CouchDbUserService;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Options;
import org.jcouchdb.document.ValueRow;
import org.jcouchdb.document.ViewResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.svenson.JSONParser;

public class TatterTest extends Assert {
    
    private Database db;
    
    @Test
    public void testFoo() throws Exception {
        
        CouchDbUserService userSvc = new CouchDbUserService();
        userSvc.setDatabase(db);
        userSvc.initialize();
        
        CouchDbTweetService svc = new CouchDbTweetService();
        svc.setDatabase(db);
        svc.initialize();
        
        Tweet tweet = new Tweet();
        tweet.setText("Hello World!");
        tweet.setDate(new Date());
        tweet.setUser("admin");
        
        svc.tweet(tweet);
        
        Tweet tweet2 = svc.getTweet(tweet.getId());
        assertNotNull(tweet2);
        assertNotNull(tweet.getId());
        assertEquals(tweet.getText(), tweet2.getText());
        
        List<Tweet> recent = svc.getRecentTweets(tweet.getUser());
        assertNotNull(recent);
        assertSame(1, recent.size());
    }
   
    @Before
    public void setupDatabase() {
        db = new Database("localhost", 5984, "tweets");
    }
    
    @After
    public void clearDatabase() {
        ViewResult<Map> docs = db.listDocuments(new Options(), new JSONParser());
        for (ValueRow<Map> row : docs.getRows()) {
            db.delete(row.getId(), (String) row.getValue().get("rev"));
        }
    }
}
