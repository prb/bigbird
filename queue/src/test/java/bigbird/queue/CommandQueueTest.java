package bigbird.queue;

import bigbird.queue.voldemort.VoldemortCommandQueue;
import bigbird.voldemort.AbstractVoldemortTest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
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
    
    @Test
    public void testNormalQueue() throws Exception {
        Command command = new CounterCommand();
        int totalCount = 200;
        for (int i = 0; i < totalCount; i ++) {
            queue.add(command);
        }
        int i = 0;
        while (counter < totalCount && i < 100) {
            Thread.sleep(100);
            i++;
        }
        assertEquals(totalCount, counter);
        
        StoreClient<String, String> client = factory.getStoreClient("commandQueues");
        assertEquals("300", client.getValue(nodeName + ":maximum"));
        assertTrue(new Long(client.getValue(nodeName + ":minimum")) > 0);
    }
    
    @Test
    public void testStoreGoesDown() throws Exception {
        Command command = new CounterCommand();
        voldemort.stop();
        stop = false;
        
        assertFalse(queue.add(command));
//        
//        StoreClient<String, String> client = factory.getStoreClient("commandQueues");
//        assertEquals("300", client.getValue(nodeName + ":maximum"));
//        assertTrue(new Long(client.getValue(nodeName + ":minimum")) > 0);
    }

    @Before
    public void setupQueue() {
        ClientConfig config = new ClientConfig();
        config.setBootstrapUrls("tcp://localhost:6666");
        
        factory = new SocketStoreClientFactory(config);
        
        queue = new VoldemortCommandQueue();
        queue.setCommandExecutor(new ThreadPoolExecutor(1, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()));
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

    protected String getVoldemortHome() {
        return "../voldemort/" + super.getVoldemortHome();
    }
    
    public static class CounterCommand extends Command implements Serializable {
        @Override
        public void execute(Map<String, Object> commandContext) {
            ((CommandQueueTest) commandContext.get("test")).increment();
        }
    }
}
