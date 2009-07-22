package bigbird.web;

import bigbird.User;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;

public class UserDetailsWrapper implements UserDetails {

    private final User user;

    public UserDetailsWrapper(User user) {
        this.user = user;
    }

    public GrantedAuthority[] getAuthorities() {
        return new GrantedAuthority[] {
            new GrantedAuthorityImpl("ROLE_USER")
        };
    }

    public String getPassword() {
        return user.getPassword();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

}
