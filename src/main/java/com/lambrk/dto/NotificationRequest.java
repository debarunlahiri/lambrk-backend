package com.lambrk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record NotificationRequest(
    
    @NotNull
    NotificationType type,
    
    @NotNull
    UUID recipientId,
    
    @NotBlank
    @Size(max = 500, message = "Title must be less than 500 characters")
    String title,
    
    @NotBlank
    @Size(max = 2000, message = "Message must be less than 2000 characters")
    String message,
    
    UUID relatedPostId,
    
    UUID relatedCommentId,
    
    UUID relatedUserId,
    
    String actionUrl,
    
    String actionText,
    
    boolean isRead
) {

    public enum NotificationType {
        COMMENT_REPLY, POST_LIKE, COMMENT_LIKE, POST_MENTION, COMMENT_MENTION,
        COMMUNITY_INVITE, MODERATOR_ACTION, SYSTEM_ANNOUNCEMENT, CONTENT_MODERATION
    }
}
