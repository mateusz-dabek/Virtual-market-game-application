package project.services;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import project.model.User;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


@Getter
public class UserDetailsImpl implements UserDetails {
    private Long id;
    private String password;
    private boolean enabled;


    public UserDetailsImpl(User user) {
        this.id = user.getId();
        this.password = user.getPassword();
        this.enabled = user.getEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() { // wykorzystywane
        return Long.toString(id);
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
