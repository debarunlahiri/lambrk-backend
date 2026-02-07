package com.lambrk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "admin_actions", indexes = {
    @Index(name = "idx_admin_action_type", columnList = "type"),
    @Index(name = "idx_admin_action_target", columnList = "target_id"),
    @Index(name = "idx_admin_action_performed_by", columnList = "performed_by"),
    @Index(name = "idx_admin_action_created_at", columnList = "created_at"),
    @Index(name = "idx_admin_action_is_active", columnList = "is_active")
})
@EntityListeners(AuditingEntityListener.class)
public record AdminAction(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    AdminActionType type,
    
    @Column(name = "target_id", nullable = false)
    Long targetId,
    
    @Column(name = "target_type", nullable = false, length = 50)
    String targetType,
    
    @NotBlank(message = "Reason is required")
    @Size(max = 1000, message = "Reason must be less than 1000 characters")
    @Column(columnDefinition = "TEXT", nullable = false)
    String reason,
    
    @Column(name = "notes", columnDefinition = "TEXT")
    String notes,
    
    @Column(name = "performed_by", nullable = false)
    Long performedBy,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt,
    
    @Column(name = "expires_at")
    Instant expiresAt,
    
    @Column(name = "is_active", nullable = false)
    boolean isActive,
    
    @Column(name = "result", length = 500)
    String result
) {
    
    public AdminAction(AdminActionType type, Long targetId, String targetType, String reason,
                      String notes, Long performedBy, Instant createdAt, Instant expiresAt,
                      boolean isActive, String result) {
        this(null, type, targetId, targetType, reason, notes, performedBy, createdAt, expiresAt, isActive, result);
    }
    
    public enum AdminActionType {
        BAN_USER, SUSPEND_USER, DELETE_POST, DELETE_COMMENT, LOCK_POST, LOCK_COMMENT,
        REMOVE_MODERATOR, ADD_MODERATOR, BAN_SUBREDDIT, QUARANTINE_POST, QUARANTINE_COMMENT
    }
}
