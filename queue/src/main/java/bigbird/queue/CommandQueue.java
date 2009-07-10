package bigbird.queue;

public interface CommandQueue {
    boolean add(Command command);
}
