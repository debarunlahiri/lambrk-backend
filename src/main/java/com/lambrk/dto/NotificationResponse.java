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
            notification.id(),
            notification.type(),
            notification.recipient().id(),
            notification.title(),
            notification.message(),
            notification.relatedPostId(),
            null, // Will be populated by service
            notification.relatedCommentId(),
            null, // Will be populated by service
            notification.relatedUserId(),
            null, // Will be populated by service
            notification.actionUrl(),
            notification.actionText(),
            notification.isRead(),
            notification.createdAt(),
            notification.readAt()
        );
    }
}
