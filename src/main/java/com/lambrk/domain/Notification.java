package com.lambrk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
    @Index(name = "idx_notification_type", columnList = "type"),
    @Index(name = "idx_notification_created_at", columnList = "created_at"),
    @Index(name = "idx_notification_is_read", columnList = "is_read")
})
@EntityListeners(AuditingEntityListener.class)
public record Notification(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    NotificationType type,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    User recipient,
    
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must be less than 500 characters")
    @Column(nullable = false, length = 500)
    String title,
    
    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must be less than 2000 characters")
    @Column(columnDefinition = "TEXT", nullable = false)
    String message,
    
    @Column(name = "related_post_id")
    Long relatedPostId,
    
    @Column(name = "related_comment_id")
    Long relatedCommentId,
    
    @Column(name = "related_user_id")
    Long relatedUserId,
    
    @Column(name = "action_url", length = 500)
    String actionUrl,
    
    @Column(name = "action_text", length = 100)
    String actionText,
    
    @Column(name = "is_read", nullable = false)
    boolean isRead,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt,
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt,
    
    @Column(name = "read_at")
    Instant readAt
) {
    
    public Notification {
        this.isRead = false;
    }
    
    public Notification(NotificationType type, User recipient, String title, String message,
                      Long relatedPostId, Long relatedCommentId, Long relatedUserId,
                      String actionUrl, String actionText, boolean isRead, Instant createdAt,
                      Instant updatedAt, Instant readAt) {
        this(null, type, recipient, title, message, relatedPostId, relatedCommentId, relatedUserId,
             actionUrl, actionText, isRead, createdAt, updatedAt, readAt);
    }
    
    public enum NotificationType {
        COMMENT_REPLY, POST_UPVOTE, COMMENT_UPVOTE, POST_MENTION, COMMENT_MENTION,
        SUBREDDIT_INVITE, MODERATOR_ACTION, SYSTEM_ANNOUNCEMENT, CONTENT_MODERATION
    }
}
