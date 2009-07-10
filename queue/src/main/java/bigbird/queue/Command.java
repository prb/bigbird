package bigbird.queue;

import java.io.Serializable;
import java.util.Map;

public abstract class Command implements Serializable {
    public abstract void execute(Map<String, Object> commandContext);
}
