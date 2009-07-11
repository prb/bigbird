package bigbird;

public interface UserService {
    void newUser(User user, String password);

    User getUser(String username);
}
