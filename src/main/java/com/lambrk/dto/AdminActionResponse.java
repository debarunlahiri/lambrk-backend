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
            action.getId(),
            action.getType(),
            action.getTargetId(),
            action.getTargetType(),
            action.getReason(),
            action.getNotes(),
            action.getPerformedBy(),
            action.getCreatedAt(),
            action.getExpiresAt(),
            action.isActive(),
            action.getResult()
        );
    }
}
