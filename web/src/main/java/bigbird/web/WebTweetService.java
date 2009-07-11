package bigbird.web;

import bigbird.BackendException;
import bigbird.Tweet;
import bigbird.TweetService;
import bigbird.UserNotFoundException;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A wrapper to TweetServices to make them a RESTful API. 
 */
@Path("/")
public class WebTweetService {
    protected final Log log = LogFactory.getLog(getClass());
    private TweetService tweetService;
    
    @GET
    @Path("/users/{user}")
    @Produces("application/json")
    public Response getTweets(@PathParam("user") String user, @QueryParam("start") int start, @QueryParam("count") int count) {
        try {
            return Response.ok().entity(toWeb(tweetService.getTweets(user, start, count))).build();
        } catch (UserNotFoundException e) {
            return Response.status(404).entity("Invalid user " + user).build();
        }
    }

    @GET
    @Produces("application/json")
    public Response getFriendsTimeline(@QueryParam("start") int start, @QueryParam("count") int count) {
        String user = getCurrentUser();
        try {
            return Response.ok().entity(toWeb(tweetService.getFriendsTimeline(user, start, count))).build();
        } catch (UserNotFoundException e) {
            return Response.status(404).entity("Invalid user " + user).build();
        }
    }

    @POST
    @Path("/tweet")
    @Consumes("application/json")
    public Response tweet(TweetRequest req) {
        if (req == null || req.getTweet() == null) {
            Response.status(400).entity("Tweet text cannot be empty.");
        }
        
        Tweet tweet = new Tweet();
        tweet.setText(req.getTweet());
        tweet.setUser(getCurrentUser());
        
        try {
            tweetService.tweet(tweet);
            return Response.ok().build();
        } catch (UserNotFoundException e) {
            return Response.status(401).build();
        } catch (BackendException e) {
            log.error(e);
            return Response.status(500).build();
        }
    }
    
    private List<WebTweet> toWeb(List<Tweet> tweets) {
        List<WebTweet> webTweets = new ArrayList<WebTweet>();
        
        for (Tweet t: tweets) {
            webTweets.add(new WebTweet(t));
        }
        
        return webTweets;
    }

    private String getCurrentUser() {
        // TODO: Get auth token from acegi
        return "admin";
    }
    
    public void setTweetService(TweetService tweetService) {
        this.tweetService = tweetService;
    }
    
    
}
