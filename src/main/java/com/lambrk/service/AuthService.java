package com.lambrk.service;

import com.lambrk.config.JwtTokenProvider;
import com.lambrk.domain.User;
import com.lambrk.dto.AuthRequest;
import com.lambrk.dto.AuthResponse;
import com.lambrk.dto.RegisterRequest;
import com.lambrk.dto.UserResponse;
import com.lambrk.exception.DuplicateResourceException;
import com.lambrk.exception.ResourceNotFoundException;
import com.lambrk.repository.UserRepository;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final CustomMetrics customMetrics;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      JwtTokenProvider tokenProvider, AuthenticationManager authenticationManager,
                      CustomMetrics customMetrics) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.customMetrics = customMetrics;
    }

    @RateLimiter(name = "userRegistration")
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("User", "username", request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        User user = new User(
            null, request.username(), request.email(),
            passwordEncoder.encode(request.password()),
            request.displayName(), null, null, true, false, 0,
            new HashSet<>(), new HashSet<>(), new HashSet<>(),
            new HashSet<>(), new HashSet<>(), Instant.now(), Instant.now()
        );

        User saved = userRepository.save(user);
        customMetrics.recordUserRegistration();

        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        String accessToken = tokenProvider.generateToken(auth);
        String refreshToken = tokenProvider.generateRefreshToken(saved.username());

        return new AuthResponse(accessToken, refreshToken, 86400, UserResponse.from(saved));
    }

    public AuthResponse login(AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", request.username()));

        String accessToken = tokenProvider.generateToken(auth);
        String refreshToken = tokenProvider.generateRefreshToken(user.username());

        customMetrics.recordUserLogin(user.id().toString());

        return new AuthResponse(accessToken, refreshToken, 86400, UserResponse.from(user));
    }

    public AuthResponse refreshToken(String refreshToken) {
        String newAccessToken = tokenProvider.refreshToken(refreshToken);
        String username = tokenProvider.getUsernameFromJWT(refreshToken);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        String newRefreshToken = tokenProvider.generateRefreshToken(username);
        return new AuthResponse(newAccessToken, newRefreshToken, 86400, UserResponse.from(user));
    }
}
