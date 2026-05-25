package com.lambrk.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

/**
 * Custom UserDetails implementation that includes the user's UUID.
 * Used to carry the user ID through the security context without database lookups.
 */
public class UserPrincipal extends User {

    private final UUID userId;

    public UserPrincipal(UUID userId, String username, String password,
                        Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
