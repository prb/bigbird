package bigbird.voldemort;

import java.util.List;
import java.util.Map;

import voldemort.client.StoreClient;
import voldemort.client.UpdateAction;
import bigbird.queue.Command;

public abstract class AbstractVoldemortCommand extends Command {

    private transient Object returnValue;
    
    public AbstractVoldemortCommand() {
        super();
    }

    @Override
    public Object execute(Map<String, Object> commandContext) {
        final VoldemortTweetService service = (VoldemortTweetService) commandContext.get(VoldemortTweetService.SERVICE);
        final StoreClient<String,List<String>> friendsTimeline 
            = (StoreClient<String,List<String>>) commandContext.get(VoldemortTweetService.FRIENDS_TIMELINE_CLIENT);
        final StoreClient<String,Map<String,String>> users 
            = (StoreClient<String,Map<String,String>>) commandContext.get(VoldemortTweetService.USERS_CLIENT);
        final StoreClient<String,Map<String,String>> tweets 
            = (StoreClient<String,Map<String,String>>) commandContext.get(VoldemortTweetService.TWEETS_CLIENT);
        
        friendsTimeline.applyUpdate(new UpdateAction<String, List<String>>() {
            @Override
            public void update(StoreClient<String, List<String>> arg0) {
                returnValue = execute(service, friendsTimeline, users, tweets);
            }
        });
        return returnValue;
    }

    protected abstract Object execute(VoldemortTweetService service,
                                      StoreClient<String, List<String>> friendsTimeline,
                                      StoreClient<String, Map<String, String>> users, 
                                      StoreClient<String, Map<String, String>> tweets);

}
