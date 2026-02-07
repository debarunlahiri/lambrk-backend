package com.lambrk.dto;

import com.lambrk.domain.User;

import java.time.Instant;

public record UserResponse(
    
    Long id,
    
    String username,
    
    String displayName,
    
    String bio,
    
    String avatarUrl,
    
    boolean isActive,
    
    boolean isVerified,
    
    int karma,
    
    Instant createdAt,
    
    Instant updatedAt
) {
    
    public static UserResponse from(User user) {
        return new UserResponse(
            user.id(),
            user.username(),
            user.displayName(),
            user.bio(),
            user.avatarUrl(),
            user.isActive(),
            user.isVerified(),
            user.karma(),
            user.createdAt(),
            user.updatedAt()
        );
    }
}
