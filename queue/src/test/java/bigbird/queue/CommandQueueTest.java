package bigbird.queue;

import bigbird.queue.voldemort.VoldemortCommandQueue;
import bigbird.voldemort.AbstractVoldemortTest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import voldemort.client.ClientConfig;
import voldemort.client.SocketStoreClientFactory;

public class CommandQueueTest extends AbstractVoldemortTest {
    
    public static class CounterCommand extends Command implements Serializable {
        @Override
        public void execute(Map<String, Object> commandContext) {
            ((CommandQueueTest) commandContext.get("test")).increment();
        }
    }

    private int counter = 0;
    
    @Test
    public void testNormalQueue() {
        ClientConfig config = new ClientConfig();
        config.setBootstrapUrls("tcp://localhost:6666");
        
        SocketStoreClientFactory factory = new SocketStoreClientFactory(config);
        
        VoldemortCommandQueue queue = new VoldemortCommandQueue();
        queue.setCommandExecutor(new ThreadPoolExecutor(1, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()));
        queue.setIndexIncrementExecutor(new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()));
        queue.setStoreClientFactory(factory);
        
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("test", this);
        queue.setCommandContext(context);
        
        Command command = new CounterCommand();
        for (int i = 0; i < 200; i ++) {
            queue.add(command);
        }
        
    }
    
    public void increment() {
        counter++;
    }

    protected String getVoldemortHome() {
        return "../voldemort/" + super.getVoldemortHome();
    }
}
