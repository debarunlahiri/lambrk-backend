package com.lambrk.controller;

import com.lambrk.dto.AuthRequest;
import com.lambrk.dto.AuthResponse;
import com.lambrk.dto.RegisterRequest;
import com.lambrk.service.AuthService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Counted(value = "auth.register", extraTags = {"type", "register"})
    @Timed(value = "auth.register.duration")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Counted(value = "auth.login", extraTags = {"type", "login"})
    @Timed(value = "auth.login.duration")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Timed(value = "auth.refresh.duration")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}
