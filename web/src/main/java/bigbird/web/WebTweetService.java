package bigbird.web;

import bigbird.Tweet;
import bigbird.TweetService;
import bigbird.UserNotFoundException;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * A wrapper to TweetServices to make them a RESTful API. 
 */
@Path("/")
public class WebTweetService {
    private TweetService tweetService;
    
    @GET
    @Path("/{user}")
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
        // TODO: Get auth token from acegi
        String user = "admin";
        try {
            return Response.ok().entity(toWeb(tweetService.getFriendsTimeline(user, start, count))).build();
        } catch (UserNotFoundException e) {
            return Response.status(404).entity("Invalid user " + user).build();
        }
    }

        
    private List<WebTweet> toWeb(List<Tweet> tweets) {
        List<WebTweet> webTweets = new ArrayList<WebTweet>();
        
        for (Tweet t: tweets) {
            webTweets.add(new WebTweet(t));
        }
        
        return webTweets;
    }

    public void setTweetService(TweetService tweetService) {
        this.tweetService = tweetService;
    }
    
    
}
