package com.lambrk.config;

import com.lambrk.domain.User;
import com.lambrk.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is disabled: " + username);
        }

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.username())
            .password(user.password())
            .disabled(!user.isActive())
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .authorities(getAuthorities(user))
            .build();
    }

    private List<SimpleGrantedAuthority> getAuthorities(User user) {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        
        if (user.isVerified()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_VERIFIED"));
        }
        
        // Add admin role for admin user
        if ("admin".equals(user.username())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_MODERATOR"));
        }
        
        return authorities;
    }
}
