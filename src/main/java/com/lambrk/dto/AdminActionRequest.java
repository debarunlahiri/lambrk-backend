package com.lambrk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AdminActionRequest(
    
    @NotNull
    AdminActionType action,
    
    @NotNull
    UUID targetId,
    
    String reason,
    
    String notes,
    
    Long durationDays,
    
    boolean permanent,
    
    boolean notifyUser
) {

    public enum AdminActionType {
        BAN_USER, SUSPEND_USER, DELETE_POST, DELETE_COMMENT, LOCK_POST, LOCK_COMMENT,
        REMOVE_MODERATOR, ADD_MODERATOR, BAN_COMMUNITY, QUARANTINE_POST, QUARANTINE_COMMENT
    }
}
