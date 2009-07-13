package bigbird.voldemort;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import voldemort.client.ClientConfig;
import voldemort.client.SocketStoreClientFactory;

public class AbstractVoldemortTest extends Assert {
    
    protected EmbeddedVoldemort voldemort;
    protected boolean stop = true;

    protected VoldemortTweetService tweetService;
    protected VoldemortUserService userService;
    protected VoldemortCommandQueue queue;
    private ThreadPoolExecutor commandExecutor;
    private String nodeName = "node1";

    @Before
    public void setUp() throws Exception {
//        voldemort = new EmbeddedVoldemort();
//        voldemort.setVoldemortHome(getVoldemortHome());
//        voldemort.start();
        
        ClientConfig config = new ClientConfig();
        config.setBootstrapUrls("tcp://localhost:6666");
        config.setMaxThreads(10);
        
        SocketStoreClientFactory factory = new SocketStoreClientFactory(config);
        
        queue = new VoldemortCommandQueue();
        commandExecutor = new ThreadPoolExecutor(1, 100, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        queue.setCommandExecutor(commandExecutor);
        queue.setIndexIncrementExecutor(new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()));
        queue.setStoreClientFactory(factory);
        queue.setNodeName(nodeName );
        
        tweetService = new VoldemortTweetService();
        tweetService.setStoreClientFactory(factory);
        tweetService.setCommandQueue(queue);
        tweetService.initialize();

        userService = new VoldemortUserService();
        userService.setStoreClientFactory(factory);
        userService.initialize();
    }
    
    protected String getVoldemortHome() {
        return "./src/test/resources";
    }

    @After
    public void tearDown() throws Exception {
        if (queue != null) {
            queue.shutdown();
        }
        
        if (voldemort != null && stop) {
            voldemort.stop();
        }
    }
}
