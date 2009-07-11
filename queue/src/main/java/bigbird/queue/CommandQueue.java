package bigbird.queue;

import java.util.Map;

public interface CommandQueue {
    /**
     * Store the command and then execute it asynchronously.
     * @param command
     * @throws CannotStoreCommandException
     */
    void addAsync(Command command) throws CannotStoreCommandException;
    
    /**
     * Store the command and execute it synchronously.
     * @param command
     * @return
     * @throws CannotStoreCommandException
     */
    Object add(Command command) throws CannotStoreCommandException;
    
    Map<String,Object> getCommandContext();
}
