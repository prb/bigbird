package bigbird;

public class BackendException extends Exception {

    public BackendException(String message, Throwable cause) {
        super(message, cause);
    }

    public BackendException(String message) {
        super(message);
    }

    public BackendException(Throwable cause) {
        super(cause);
    }


}
