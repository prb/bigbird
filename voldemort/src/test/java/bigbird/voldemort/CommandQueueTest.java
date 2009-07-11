package bigbird.voldemort;

import bigbird.queue.CannotStoreCommandException;
import bigbird.queue.Command;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import voldemort.client.ClientConfig;
import voldemort.client.SocketStoreClientFactory;
import voldemort.client.StoreClient;

public class CommandQueueTest extends AbstractVoldemortTest {
    private String nodeName = "test";
    private int counter = 0;
    private SocketStoreClientFactory factory;
    private VoldemortCommandQueue queue;
    private ThreadPoolExecutor commandExecutor;
    
    @Test
    public void testNormalQueue() throws Exception {
        Command command = new CounterCommand();
        int totalCount = 200;
        for (int i = 0; i < totalCount; i ++) {
            queue.addAsync(command);
        }
        
        waitForCount(totalCount);
        assertEquals(totalCount, counter);
        
        StoreClient<String, String> client = getQueueClient();
        assertEquals("300", client.getValue(nodeName + ":maximum"));
        assertTrue(new Long(client.getValue(nodeName + ":minimum")) > 0);
    }

    private void waitForCount(int totalCount) throws InterruptedException {
        // ensure we at least wait a little bit
        int loop = Math.max(10, totalCount);
        
        int i = 0;
        while (counter < totalCount && i < loop) {
            Thread.sleep(100);
            i++;
        }
    }

    private StoreClient<String, String> getQueueClient() {
        StoreClient<String, String> client = factory.getStoreClient("commandQueues");
        return client;
    }
    
    @Test
    public void testStoreGoesDown() throws Exception {
        Command command = new CounterCommand();
        voldemort.stop();
        stop = false;
        
        try {
            queue.add(command);
            fail("Shoult not be able to store command");
        } catch (CannotStoreCommandException e) {
            // expected
        }
//        
//        StoreClient<String, String> client = factory.getStoreClient("commandQueues");
//        assertEquals("300", client.getValue(nodeName + ":maximum"));
//        assertTrue(new Long(client.getValue(nodeName + ":minimum")) > 0);
    }

    
    /**
     * Simulate commands never being finished by stopping the executor and adding commands to it.
     * When the store comes back up (or is reinitialized), they should get done.
     * @throws Exception
     */
    @Test
    public void testCommandsNeverGetExecuted() throws Exception {
        Command command = new CounterCommand();

        queue.setCommandExecutor(new Executor() {

            public void execute(Runnable command) {
                // don't do anything to simulate failure
            }
            
        });
        
        queue.addAsync(command);
        
        assertEquals(0, counter);
        
        queue.shutdown();
        
        StoreClient<String, String> client = getQueueClient();
        assertEquals("100", client.getValue(nodeName + ":maximum"));
        assertEquals("0", client.getValue(nodeName + ":minimum"));
        
        queue.setCommandExecutor(commandExecutor);
        queue.initialize();
        
        waitForCount(1);
        assertEquals(1, counter);
        
        queue.shutdown();
        
        Thread.sleep(1000);
        
        assertEquals("100", client.getValue(nodeName + ":maximum"));
        assertEquals("1", client.getValue(nodeName + ":minimum"));
    }
    
    @Before
    public void setupQueue() {
        ClientConfig config = new ClientConfig();
        config.setBootstrapUrls("tcp://localhost:6666");
        
        factory = new SocketStoreClientFactory(config);
        
        queue = new VoldemortCommandQueue();
        commandExecutor = new ThreadPoolExecutor(1, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        queue.setCommandExecutor(commandExecutor);
        queue.setIndexIncrementExecutor(new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()));
        queue.setStoreClientFactory(factory);
        queue.setNodeName(nodeName);
        
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("test", this);
        queue.setCommandContext(context);
    }
    
    public void increment() {
        counter++;
    }
    
    public static class CounterCommand extends Command implements Serializable {
        @Override
        public Object execute(Map<String, Object> commandContext) {
            ((CommandQueueTest) commandContext.get("test")).increment();
            return null;
        }
    }
}
