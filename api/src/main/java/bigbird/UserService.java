package bigbird;

public interface UserService {
    void newUser(User user) throws AlreadyExistsException;

    User getUser(String username);
}
