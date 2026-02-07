package com.lambrk.dto;

import com.lambrk.domain.AdminAction;
import java.time.Instant;

public record AdminActionResponse(
    
    Long actionId,
    
    AdminActionRequest.AdminActionType action,
    
    Long targetId,
    
    String targetType,
    
    String reason,
    
    String notes,
    
    Long performedBy,
    
    Instant performedAt,
    
    Instant expiresAt,
    
    boolean isActive,
    
    String result
) {
    
    public static AdminActionResponse from(AdminAction action) {
        return new AdminActionResponse(
            action.id(),
            action.type(),
            action.targetId(),
            action.targetType(),
            action.reason(),
            action.notes(),
            action.performedBy(),
            action.createdAt(),
            action.expiresAt(),
            action.isActive(),
            action.result()
        );
    }
}
