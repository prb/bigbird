package bigbird;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String id) {
        super(id);
    }

}
