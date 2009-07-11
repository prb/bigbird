package bigbird.voldemort;


import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import voldemort.client.StoreClient;
import voldemort.versioning.Versioned;

/**
 * 1. Update the list of followers for the user
 * 2. Update the list of people following the newly followed user
 * 3. Recompute the user's timeline
 */
public class StopFollowingCommand extends AbstractVoldemortCommand {

    private String userId;
    private String toStopUser;
    
    public StopFollowingCommand(String userId, String follow) {
        super();
        this.userId = userId;
        this.toStopUser = follow;
    }

    @Override
    protected Object execute(VoldemortTweetService service,
                             StoreClient<String, List<String>> friendsTimeline,
                             StoreClient<String, Map<String, String>> users, 
                             StoreClient<String, Map<String, String>> tweets) {
        updateFollowers(users);
        
        updateFollowing(users);
        
        Versioned<List<String>> versioned = friendsTimeline.get(userId);
        List<String> timeline;
        if (versioned == null) {
            return null;
        } else {
            timeline = versioned.getValue();
        }

        for (Iterator itr = timeline.iterator(); itr.hasNext();) {
            String idAndTime = (String)itr.next();
            
            String[] split = idAndTime.split(":");
            String id = split[0];
            
            if (id.startsWith(toStopUser + "-")) {
                itr.remove();
            }
        }
        
        friendsTimeline.put(userId, versioned);

         // TODO: add in more tweets from followers
            
        return null;
    }

    private void updateFollowing(StoreClient<String, Map<String, String>> users) {
        Versioned<Map<String, String>> versionedUser = users.get(userId);
        Map<String, String> user = versionedUser.getValue();
        Set<String> followers = VoldemortTweetService.asSet(user.get(VoldemortTweetService.FOLLOWING));
        
        followers.remove(toStopUser);
        user.put(VoldemortTweetService.FOLLOWING, VoldemortTweetService.toString(followers));
        
        // Update the follower information
        users.put(userId, versionedUser);
    }

    private void updateFollowers(StoreClient<String, Map<String, String>> users) {
        Versioned<Map<String, String>> versionedUser = users.get(toStopUser);
        Map<String, String> user = versionedUser.getValue();
        Set<String> followers = VoldemortTweetService.asSet(user.get(VoldemortTweetService.FOLLOWERS));
        
        followers.remove(userId);
        user.put(VoldemortTweetService.FOLLOWERS, VoldemortTweetService.toString(followers));
        
        // Update the follower information
        users.put(toStopUser, versionedUser);
    }
    
}
