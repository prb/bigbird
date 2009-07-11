package bigbird.web;

import bigbird.User;
import bigbird.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;

public class UserDetailsService implements org.springframework.security.userdetails.UserDetailsService {
    private UserService userService;
    
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException,
        DataAccessException {
        User user = userService.getUser(username);
        
        if (user == null) {
            throw new UsernameNotFoundException("User " + username + " is invalid");
        }
        
        return new UserDetailsWrapper(user);
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

}
