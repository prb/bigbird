package bigbird.voldemort;

import bigbird.AlreadyExistsException;
import bigbird.User;
import bigbird.UserService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import voldemort.client.StoreClient;
import voldemort.client.StoreClientFactory;
import voldemort.client.UpdateAction;
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
            user.setPassword("admin");
            user.setName("Administrator");
            user.setCreated(new Date());
            newUser(user);
        }
    }
    
    public void newUser(final User user) throws AlreadyExistsException {
        final Map<String,String> userMap = new HashMap<String, String>();
        userMap.put(USERNAME, user.getUsername());
        userMap.put(PASSWORD, user.getPassword());
        userMap.put(NAME, user.getName());
        userMap.put(CREATED, new Long(new Date().getTime()).toString());
        userMap.put(LAST_TWEET, "0");
        
        boolean success = users.applyUpdate(new UpdateAction<String, Map<String,String>>() {

            @Override
            public void update(StoreClient<String, Map<String, String>> users) {
                users.put(user.getUsername(), new Versioned(userMap));
            }
            
        });
        
        if (!success) {
            throw new AlreadyExistsException();
        }
    }

    public User getUser(String username) {
        Map<String, String> value = users.getValue(username);
        if (value != null) {
            User user = new User();
            user.setUsername(username);
            user.setName(value.get(NAME));
            user.setPassword(value.get(PASSWORD));
            user.setCreated(new Date(new Long(value.get(CREATED))));
            return user;
        }
        return null;
    }

    public void setStoreClientFactory(StoreClientFactory storeClientFactory) {
        users = storeClientFactory.getStoreClient("users");
    }
}
