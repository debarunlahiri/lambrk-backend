package com.lambrk.dto;

import com.lambrk.domain.Notification;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    
    UUID id,
    
    Notification.NotificationType type,
    
    UUID recipientId,
    
    String title,
    
    String message,
    
    UUID relatedPostId,
    
    String relatedPostTitle,
    
    UUID relatedCommentId,
    
    String relatedCommentPreview,
    
    UUID relatedUserId,
    
    String relatedUsername,
    
    String actionUrl,
    
    String actionText,
    
    boolean isRead,
    
    Instant createdAt,
    
    Instant readAt
) {
    
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getType(),
            notification.getRecipient().getId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getRelatedPostId(),
            null, // Will be populated by service
            notification.getRelatedCommentId(),
            null, // Will be populated by service
            notification.getRelatedUserId(),
            null, // Will be populated by service
            notification.getActionUrl(),
            notification.getActionText(),
            notification.isRead(),
            notification.getCreatedAt(),
            notification.getReadAt()
        );
    }
}
