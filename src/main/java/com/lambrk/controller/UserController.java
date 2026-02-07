package com.lambrk.controller;

import com.lambrk.dto.UserResponse;
import com.lambrk.domain.User;
import com.lambrk.exception.ResourceNotFoundException;
import com.lambrk.repository.UserRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{userId}")
    @NewSpan("get-user")
    @Timed(value = "users.get.duration")
    public ResponseEntity<UserResponse> getUser(@PathVariable @SpanTag Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/username/{username}")
    @NewSpan("get-user-by-username")
    @Timed(value = "users.getByUsername.duration")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable @SpanTag String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/me")
    @NewSpan("get-current-user")
    @Timed(value = "users.me.duration")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", userDetails.getUsername()));
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/top")
    @NewSpan("get-top-users")
    @Timed(value = "users.top.duration")
    public ResponseEntity<Page<UserResponse>> getTopUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "karma"));
        return ResponseEntity.ok(userRepository.findTopUsersByKarma(pageable).map(UserResponse::from));
    }

    @GetMapping("/search")
    @NewSpan("search-users")
    @Timed(value = "users.search.duration")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam @SpanTag String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userRepository.searchActiveUsers(query, pageable).map(UserResponse::from));
    }

    @DeleteMapping("/{userId}")
    @NewSpan("delete-user")
    @Timed(value = "users.delete.duration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable @SpanTag Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }
}
