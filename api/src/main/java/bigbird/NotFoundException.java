package bigbird;

public class NotFoundException extends Exception {

    public NotFoundException(String id) {
        super("Object with ID '" + id + "' was not found.");
    }

}
