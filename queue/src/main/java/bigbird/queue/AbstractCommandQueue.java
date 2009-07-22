package bigbird.queue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Idea here is that following happens:
 * 1. Store the command you want to execute. This will be persisted reliably upon multiple servers.
 * 2. Threads in the background will pull off commands and execute them.
 * <br/>
 * Commands must be idempotent as there is chance that they may get executed again on restart.
 * <br/>
 * Failure Scenarios:
 * <ul>
 * <li>
 * Machine executing commands dies:
 *   This is the same machine as the CommandQueue. When it comes back online, it will restore
 *   the queue and begin processing commands.
 * </li>
 * <li>  
 * Remote storage dies:
 *   add() will throw an error and will not allow new commands to be added.
 *   commands which are currently executing and could not be deleted will be run again.
 * </li>
 */
public abstract class AbstractCommandQueue implements CommandQueue {
    protected final Log log = LogFactory.getLog(getClass());
    
    protected String nodeName;
    
    protected long increment = 100;
    protected long maximum = 100;
    protected long minimum = 0;
    private long threshold = 10;
    
    private long nextId;
    
    protected Executor commandExecutor;
    private Executor indexIncrementExecutor;
    
    private boolean gettingNextBatch = false;
    
    protected Map<String,Object> commandContext = new HashMap<String, Object>();
    
    public Object add(final Command command) throws CannotStoreCommandException {
        final long commandId = storeRemotely(command);
        
        if (commandId != -1) {
            return command.execute(commandContext);
        } else {
            throw new CannotStoreCommandException();
        }
    }
    

    public void addAsync(final Command command) throws CannotStoreCommandException {
        final long commandId = storeRemotely(command);
        
        if (commandId != -1) {
            addCommand(command, commandId);
        } else {
            throw new CannotStoreCommandException();
        }
    }

    protected void addCommand(final Command command, final long commandId) {
        commandExecutor.execute(new Runnable() {
            public void run() {
                long start = System.currentTimeMillis();
                command.execute(commandContext);
                finishCommand(commandId);
                
//                if (log.isInfoEnabled()) {
                    System.out.println("Executed " + command.toString() + " in " + (System.currentTimeMillis() - start) + " ms");
//                }
            }
        });
    }
    
    protected synchronized long getNextId() {
        if (maximum - threshold <= nextId && !gettingNextBatch) {
            gettingNextBatch = true;
            indexIncrementExecutor.execute(new Runnable() {
                public void run() {
                    getNextBatch();
                    gettingNextBatch = false;
                }
            });
        }
        
        return nextId++;
    }

    protected byte[] serialize(Command command) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOut = new ObjectOutputStream(bos);
            objectOut.writeObject(command);
            objectOut.close();
            return bos.toByteArray();
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    protected abstract long storeRemotely(Command command);

    protected abstract void getNextBatch();

    protected void finishCommand(long commandId) {
        delete(commandId); 
        
        if (minimum <= commandId) {
            minimum = commandId + 1;
        }
    }
    
    protected abstract void delete(long commandId);

    public void setNodeName(String node) {
        this.nodeName = node;
    }

    public Map<String, Object> getCommandContext() {
        return commandContext;
    }


    public void setCommandContext(Map<String, Object> commandContext) {
        this.commandContext = commandContext;
    }

    public void setIncrement(long increment) {
        this.increment = increment;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }

    public void setCommandExecutor(Executor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public void setIndexIncrementExecutor(Executor indexIncrementExecutor) {
        this.indexIncrementExecutor = indexIncrementExecutor;
    }
    
    public Executor getCommandExecutor() {
        return commandExecutor;
    }
    
}
