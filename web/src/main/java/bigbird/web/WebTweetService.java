package bigbird.web;

import bigbird.AlreadyExistsException;
import bigbird.BackendException;
import bigbird.Tweet;
import bigbird.TweetService;
import bigbird.User;
import bigbird.UserNotFoundException;
import bigbird.UserService;

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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

/**
 * A wrapper to TweetServices to make them a RESTful API. 
 */
@Path("/")
public class WebTweetService {
    protected final Log log = LogFactory.getLog(getClass());
    private TweetService tweetService;
    private UserService userService;
    
    @GET
    @Path("/users/{user}")
    @Produces("application/json")
    public Response getTweets(@PathParam("user") String user, 
                              @QueryParam("start") int start,
                              @QueryParam("count") int count) {
        try {
            return Response.ok().entity(toWeb(tweetService.getTweets(user, start, count))).build();
        } catch (UserNotFoundException e) {
            return Response.status(404).entity("Invalid user " + user).build();
        }
    }

    @GET
    @Produces("application/json")
    @Path("/friendsTimeline")
    public Response getFriendsTimeline(@QueryParam("start") int start, @QueryParam("count") int count) {
        String user = getCurrentUser();
        try {
            return Response.ok().entity(toWeb(tweetService.getFriendsTimeline(user, start, count))).build();
        } catch (UserNotFoundException e) {
            return Response.status(404).entity("Invalid user " + user).build();
        }
    }


    @GET
    @Produces("application/json")
    @Path("/followers")
    public Response getFollowers(@QueryParam("user") String user) {
        if (StringUtils.isEmpty(user)) {
            user = getCurrentUser();
        }
        try {
            return Response.ok().entity(tweetService.getFollowers(user)).build();
        } catch (UserNotFoundException e) {
            return Response.status(404).entity("Invalid user " + user).build();
        }
    }

    @GET
    @Produces("application/json")
    @Path("/following")
    public Response getFollowing(@QueryParam("user") String user) {
        if (StringUtils.isEmpty(user)) {
            user = getCurrentUser();
        }
        try {
            return Response.ok().entity(tweetService.getFollowing(user)).build();
        } catch (UserNotFoundException e) {
            return Response.status(404).entity("Invalid user " + user).build();
        }
    }

    @POST
    @Path("/startFollowing")
    @Consumes("application/json")
    @Produces("application/json")
    public Response startFollowing(FollowRequest req) {
        try {
            tweetService.startFollowing(getCurrentUser(), req.getUser());
            return Response.ok().build();
        } catch (UserNotFoundException e) {
            return Response.status(404).entity("Invalid user " + req.getUser()).build();
        } catch (BackendException e) {
            log.error(e);
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/stopFollowing")
    @Consumes("application/json")
    @Produces("application/json")
    public Response stopFollowing(FollowRequest req) {
        try {
            tweetService.stopFollowing(getCurrentUser(), req.getUser());
            return Response.ok().build();
        } catch (UserNotFoundException e) {
            return Response.status(404).entity("Invalid user " + req.getUser()).build();
        } catch (BackendException e) {
            log.error(e);
            return Response.status(500).entity(e.getMessage()).build();
        }
    }
    
    @POST
    @Path("/register")
    @Consumes("application/json")
    @Produces("application/json")
    public Response register(WebUser req) {
        if (req == null) {
            Response.status(400).entity("request cannot be empty.");
        }
        
        if (StringUtils.isEmpty(req.getUsername())) {
            Response.status(400).entity("Username cannot be empty.");
        }
        
        if (StringUtils.isEmpty(req.getPassword())) {
            Response.status(400).entity("Password cannot be empty.");
        }
        
        if (StringUtils.isEmpty(req.getName())) {
            Response.status(400).entity("Name cannot be empty.");
        }
        
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setName(req.getName());
        
        try {
            userService.newUser(user);
            return Response.ok().build();
        } catch (AlreadyExistsException e) {
            return Response.status(409).build();
        }
    }
    
    @POST
    @Path("/tweet")
    @Consumes("application/json")
    @Produces("application/json")
    public Response tweet(TweetRequest req) {
        if (req == null || req.getTweet() == null) {
            Response.status(400).entity("Tweet text cannot be empty.");
        }
        
        Tweet tweet = new Tweet();
        tweet.setText(req.getTweet());
        tweet.setUser(getCurrentUser());
        
        try {
            tweetService.tweet(tweet);
            return Response.ok().entity(new WebTweet(tweet)).build();
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

    public static String getCurrentUser() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        if (ctx == null) {
            return null;
        }
        Authentication auth = ctx.getAuthentication();
        if (auth == null) {
            return null;
        }
        
        UserDetailsWrapper wrapper = (UserDetailsWrapper) auth.getPrincipal();
        if (wrapper == null) {
            return null;
        }
        return wrapper.getUsername();
    }
    
    public void setTweetService(TweetService tweetService) {
        this.tweetService = tweetService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    
}
