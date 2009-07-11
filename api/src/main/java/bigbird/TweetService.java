package bigbird;

import java.util.List;
import java.util.Set;

public interface TweetService {
    List<Tweet> getRecentTweets(String user) throws UserNotFoundException;

    List<Tweet> getTweets(String user, int start, int count) throws UserNotFoundException;

    /**
     * Get tweets of all the users that this user is following.
     * @param user
     * @param start
     * @param count
     * @return
     * @throws UserNotFoundException
     */
    List<Tweet> getFriendsTimeline(String user, int start, int count) throws UserNotFoundException;

    List<Tweet> search(String text, int start, int count);
 
    Tweet getTweet(String id) throws NotFoundException;
    
    Set<String> getFollowers(String user) throws UserNotFoundException;
    
    Set<String> getFollowing(String user) throws UserNotFoundException;
    
    
    void tweet(Tweet tweet) throws UserNotFoundException, BackendException;
    
    void delete(String id) throws NotFoundException, BackendException;
    
    void startFollowing(String user, String toStartUser) throws UserNotFoundException, BackendException;

    void stopFollowing(String user, String toStopUser) throws UserNotFoundException, BackendException;
}
