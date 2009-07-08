package bigbird.voldemort;

import bigbird.User;
import bigbird.UserService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import voldemort.client.StoreClient;
import voldemort.client.StoreClientFactory;
import voldemort.versioning.Versioned;

public class VoldemortUserService implements UserService {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String NAME = "name";
    private static final String CREATED = "created";
    private static final String LAST_TWEET = "lastTweet";
    
    private StoreClient<String, Map<String,String>> users;
    
    public void initialize() throws Exception {
        Map<String, String> userMap = users.getValue("admin");
        if (userMap == null) {
            User user = new User();
            user.setUsername("admin");
            user.setName("Administrator");
            user.setCreated(new Date());
            newUser(user, "admin");
        }
    }
    
    public void newUser(User user, String password) {
        Map<String,String> userMap = new HashMap<String, String>();
        userMap.put(USERNAME, user.getUsername());
        userMap.put(PASSWORD, password);
        userMap.put(NAME, user.getName());
        userMap.put(CREATED, new Long(new Date().getTime()).toString());
        userMap.put(LAST_TWEET, "0");
        
        users.put(user.getUsername(), userMap);
        
        Versioned<Map<String, String>> versioned = users.get(user.getUsername());
        
        versioned.toString();
    }

    public void setStoreClientFactory(StoreClientFactory storeClientFactory) {
        users = storeClientFactory.getStoreClient("users");
    }
}
