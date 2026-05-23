package com.lambrk.dto;

import com.lambrk.domain.AdminAction;
import java.time.Instant;
import java.util.UUID;

public record AdminActionResponse(
    
    UUID actionId,

    AdminAction.AdminActionType action,

    UUID targetId,

    String targetType,

    String reason,

    String notes,

    UUID performedBy,
    
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
