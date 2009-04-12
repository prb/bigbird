package com.envoisolutions.tatter.voldemort;

import bigbird.UserService;

import java.util.HashMap;
import java.util.Map;

import voldemort.client.StoreClient;
import voldemort.client.StoreClientFactory;

public class VoldemortUserService implements UserService {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String LAST_TWEET = "lastTweet";
    
    private StoreClient<String, Map<String,String>> users;
    
    public void initialize() throws Exception {
        Map<String, String> user = users.getValue("admin");
        if (user == null) {
            user = new HashMap<String, String>();
            user.put(USERNAME, "admin");
            user.put(PASSWORD, "admin");
            user.put(LAST_TWEET, "0");
            users.put("admin", user);
        }
    }
    
    public void setStoreClientFactory(StoreClientFactory storeClientFactory) {
        users = storeClientFactory.getStoreClient("users");
    }
}
