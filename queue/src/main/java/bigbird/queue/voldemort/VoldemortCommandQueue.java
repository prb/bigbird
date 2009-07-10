package bigbird.queue.voldemort;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import bigbird.queue.AbstractCommandQueue;
import bigbird.queue.Command;
import voldemort.client.StoreClient;
import voldemort.client.StoreClientFactory;
import voldemort.client.UpdateAction;

public class VoldemortCommandQueue extends AbstractCommandQueue {

    private StoreClient<String, String> client;
    
    public void initialize() throws Exception {
        String maxStr = client.getValue(nodeName + ":maximum");
        if (maxStr != null) {
            maximum = Long.valueOf(maxStr);
        } else {
            return;
        }
        
        String minStr = client.getValue(nodeName + ":minimum");
        if (minStr != null) minimum = Long.valueOf(minStr);
        
        Runnable runnable = new Runnable() {
            public void run() {
                for (long l = minimum; l < maximum; l++) {
                    // Restore commands
                    String value = client.getValue(nodeName + ":" + l);
                    try {
                        ObjectInputStream objectStream = new ObjectInputStream(new ByteArrayInputStream(value.getBytes()));
                        addCommand((Command)objectStream.readObject(), l);
                    } catch (IOException e) {
                        log.error(e);
                    } catch (ClassNotFoundException e) {
                        log.error(e);
                    }
                }
            }
        };
        commandExecutor.execute(runnable);
    }
    
    protected long storeRemotely(Command command) {
        final byte[] serializedCommand = serialize(command);
        
        final long id = getNextId();
        
        try {
            boolean success = client.applyUpdate(new UpdateAction<String, String>() {
                @Override
                public void update(StoreClient<String, String> client) {
                    String nodeAndId = getVoldemortKey(id);
                    
                    client.put(nodeAndId, new String(serializedCommand));
                }
            });
            
            if (!success) {
                return -1;
            }
            
            return id;
        } catch (Exception e) {
            log.error(e);
            return -1;
        }
    }
    
    @Override
    protected void delete(final long commandId) {
        client.applyUpdate(new UpdateAction<String, String>() {
            @Override
            public void update(StoreClient<String, String> client) {
                client.delete(getVoldemortKey(commandId));
            }
        });
    }

    protected String getVoldemortKey(final long id) {
        return nodeName + ":" + new Long(id).toString();
    }
    

    @Override
    protected void getNextBatch() {
        client.applyUpdate(new UpdateAction<String, String>() {
            @Override
            public void update(StoreClient<String, String> client) {
                long newMaximum = maximum + increment;
                String maximumKey = nodeName + ":maximum";
                String minimumKey = nodeName + ":minimum";
                
                client.put(maximumKey, new Long(newMaximum).toString());
                client.put(minimumKey, new Long(minimum).toString());
                maximum = newMaximum;
            }
        });
    }

    protected String toString(long l) {
        return new Long(l).toString();
    }

    public void setStoreClientFactory(StoreClientFactory storeClientFactory) {
        client = storeClientFactory.getStoreClient("commandQueues");
    }
}
