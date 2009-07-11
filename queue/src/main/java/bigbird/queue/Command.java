package bigbird.queue;

import java.io.Serializable;
import java.util.Map;

public abstract class Command implements Serializable {
    public abstract Object execute(Map<String, Object> commandContext);
}
