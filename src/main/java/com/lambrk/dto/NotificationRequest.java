package com.lambrk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationRequest(
    
    @NotNull
    NotificationType type,
    
    @NotNull
    Long recipientId,
    
    @NotBlank
    @Size(max = 500, message = "Title must be less than 500 characters")
    String title,
    
    @NotBlank
    @Size(max = 2000, message = "Message must be less than 2000 characters")
    String message,
    
    Long relatedPostId,
    
    Long relatedCommentId,
    
    Long relatedUserId,
    
    String actionUrl,
    
    String actionText,
    
    boolean isRead
) {
    
    public NotificationRequest {
        this.isRead = false;
    }
    
    public enum NotificationType {
        COMMENT_REPLY, POST_UPVOTE, COMMENT_UPVOTE, POST_MENTION, COMMENT_MENTION,
        SUBREDDIT_INVITE, MODERATOR_ACTION, SYSTEM_ANNOUNCEMENT, CONTENT_MODERATION
    }
}
