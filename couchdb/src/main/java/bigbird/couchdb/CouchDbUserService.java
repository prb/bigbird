package bigbird.couchdb;

import bigbird.User;
import bigbird.UserService;

import java.util.HashMap;
import java.util.Map;

import org.jcouchdb.db.Database;
import org.jcouchdb.exception.NotFoundException;

public class CouchDbUserService implements UserService {

    private static final String USERNAME = "_id";
    private static final String PASSWORD = "password";
    private static final String LAST_TWEET = "lastTweet";
    
    private Database db;

    public void initialize() throws Exception {
        try {
            db.getDocument(Map.class, "admin");
        } catch (NotFoundException e) {
            HashMap<String, String> admin = new HashMap<String, String>();
            admin.put(USERNAME, "admin");
            admin.put(PASSWORD, "admin");
            admin.put("type", "user");
            admin.put(LAST_TWEET, "0");
            
            db.createDocument(admin);
        }
    }
    
    public void newUser(User user) {
        // TODO Auto-generated method stub
    }

    public User getUser(String username) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setDatabase(Database db) {
        this.db = db;
    }
    
}
