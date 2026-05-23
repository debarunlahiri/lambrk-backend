package com.lambrk.service;

import com.lambrk.domain.Notification;
import com.lambrk.domain.Comment;
import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.dto.NotificationRequest;
import com.lambrk.dto.NotificationResponse;
import com.lambrk.repository.NotificationRepository;
import com.lambrk.repository.CommentRepository;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final KafkaTemplate kafkaTemplate;
    private final CustomMetrics customMetrics;

    public NotificationService(NotificationRepository notificationRepository,
                              UserRepository userRepository,
                              CommentRepository commentRepository,
                              PostRepository postRepository,
                              KafkaTemplate kafkaTemplate,
                              CustomMetrics customMetrics) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.customMetrics = customMetrics;
    }

    @CacheEvict(value = "notifications", key = "#request.recipientId()")
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public NotificationResponse createNotification(NotificationRequest request) {
        User recipient = userRepository.findById(request.recipientId())
            .orElseThrow(() -> new RuntimeException("User not found: " + request.recipientId()));

        Notification notification = new Notification(
            null,
            Notification.NotificationType.valueOf(request.type().name()),
            recipient,
            request.title(),
            request.message(),
            request.relatedPostId(),
            request.relatedCommentId(),
            request.relatedUserId(),
            request.actionUrl(),
            request.actionText(),
            request.isRead(),
            Instant.now(),
            Instant.now(),
            null
        );

        Notification saved = notificationRepository.save(notification);
        
        // Send real-time notification via WebSocket/Kafka
        sendRealTimeNotification(saved);
        
        // Update metrics
        customMetrics.recordNotificationCreated(request.type().name());
        
        return convertToResponse(saved);
    }

    @Cacheable(value = "notifications", key = "#userId")
    public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::convertToResponse);
    }

    @Cacheable(value = "notifications", key = "#userId + '-unread'")
    public Page<NotificationResponse> getUnreadNotifications(UUID userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(userId, false, pageable);
        return notifications.map(this::convertToResponse);
    }

    @CacheEvict(value = "notifications", allEntries = true)
    public void markNotificationAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        
        if (!notification.recipient().id().equals(userId)) {
            throw new RuntimeException("Notification does not belong to user: " + userId);
        }
        
        Notification updated = new Notification(
            notification.id(),
            notification.type(),
            notification.recipient(),
            notification.title(),
            notification.message(),
            notification.relatedPostId(),
            notification.relatedCommentId(),
            notification.relatedUserId(),
            notification.actionUrl(),
            notification.actionText(),
            true,
            notification.createdAt(),
            Instant.now(),
            Instant.now()
        );
        
        notificationRepository.save(updated);
    }

    @CacheEvict(value = "notifications", key = "#userId")
    public void markAllNotificationsAsRead(UUID userId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndIsRead(userId, false);
        
        for (Notification notification : unreadNotifications) {
            Notification updated = new Notification(
                notification.id(),
                notification.type(),
                notification.recipient(),
                notification.title(),
                notification.message(),
                notification.relatedPostId(),
                notification.relatedCommentId(),
                notification.relatedUserId(),
                notification.actionUrl(),
                notification.actionText(),
                true,
                notification.createdAt(),
                Instant.now(),
                Instant.now()
            );
            notificationRepository.save(updated);
        }
    }

    @CacheEvict(value = "notifications", key = "#userId")
    public void deleteNotification(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        
        if (!notification.recipient().id().equals(userId)) {
            throw new RuntimeException("Notification does not belong to user: " + userId);
        }
        
        notificationRepository.delete(notification);
    }

    @CacheEvict(value = "notifications", key = "#userId")
    public void deleteAllNotifications(UUID userId) {
        List<Notification> notifications = notificationRepository.findByRecipientId(userId);
        notificationRepository.deleteAll(notifications);
    }

    public void createCommentReplyNotification(UUID commentId, UUID postId, UUID authorId) {
        try {
            Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));
            Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
            User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found: " + authorId));
            
            // Don't notify if user is replying to their own comment
            if (comment.parent() != null && comment.parent().author().id().equals(authorId)) {
                return;
            }
            
            UUID recipientId = comment.parent() != null ? comment.parent().author().id() : post.author().id();
            
            NotificationRequest notification = new NotificationRequest(
                NotificationRequest.NotificationType.COMMENT_REPLY,
                recipientId,
                "New reply to your comment",
                String.format("%s replied: \"%s\"", author.username(), 
                    comment.content().length() > 100 ? comment.content().substring(0, 100) + "..." : comment.content()),
                postId,
                commentId,
                authorId,
                "/posts/" + postId + "#comment-" + commentId,
                "View reply",
                false
            );
            
            createNotification(notification);
            
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to create comment reply notification: " + e.getMessage());
        }
    }

    public void createLikeNotification(UUID postId, UUID voterId, UUID authorId) {
        if (voterId.equals(authorId)) {
            return; // Don't notify for self-votes
        }

        try {
            Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
            User voter = userRepository.findById(voterId)
                .orElseThrow(() -> new RuntimeException("Voter not found: " + voterId));

            NotificationRequest notification = new NotificationRequest(
                NotificationRequest.NotificationType.POST_LIKE,
                authorId,
                "Your post received a like",
                String.format("%s liked your post \"%s\"", voter.username(), post.title()),
                postId,
                null,
                voterId,
                "/posts/" + postId,
                "View post",
                false
            );

            createNotification(notification);

        } catch (Exception e) {
            System.err.println("Failed to create like notification: " + e.getMessage());
        }
    }

    public void createMentionNotification(String content, UUID postId, UUID commentId, UUID mentionedUserId, UUID authorId) {
        if (mentionedUserId.equals(authorId)) {
            return; // Don't notify for self-mentions
        }
        
        try {
            User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found: " + authorId));
            
            NotificationRequest notification = new NotificationRequest(
                NotificationRequest.NotificationType.POST_MENTION,
                mentionedUserId,
                "You were mentioned in a post",
                String.format("%s mentioned you in \"%s\"", author.username(), 
                    content.length() > 100 ? content.substring(0, 100) + "..." : content),
                postId,
                commentId,
                authorId,
                commentId != null ? "/posts/" + postId + "#comment-" + commentId : "/posts/" + postId,
                "View mention",
                false
            );
            
            createNotification(notification);
            
        } catch (Exception e) {
            System.err.println("Failed to create mention notification: " + e.getMessage());
        }
    }

    private void sendRealTimeNotification(Notification notification) {
        try {
            // Send to WebSocket topic
            kafkaTemplate.send("notifications", notification);
            
            // Could also send to specific user topic
            kafkaTemplate.send("user-" + notification.recipient().id() + "-notifications", notification);
            
        } catch (Exception e) {
            System.err.println("Failed to send real-time notification: " + e.getMessage());
        }
    }

    private NotificationResponse convertToResponse(Notification notification) {
        return new NotificationResponse(
            notification.id(),
            notification.type(),
            notification.recipient().id(),
            notification.title(),
            notification.message(),
            notification.relatedPostId(),
            getPostTitle(notification.relatedPostId()),
            notification.relatedCommentId(),
            getCommentPreview(notification.relatedCommentId()),
            notification.relatedUserId(),
            getUsername(notification.relatedUserId()),
            notification.actionUrl(),
            notification.actionText(),
            notification.isRead(),
            notification.createdAt(),
            notification.readAt()
        );
    }

    private String getPostTitle(UUID postId) {
        return postRepository.findById(postId)
            .map(Post::title)
            .orElse(null);
    }

    private String getCommentPreview(UUID commentId) {
        return commentRepository.findById(commentId)
            .map(c -> c.content().length() > 100 ? c.content().substring(0, 100) + "..." : c.content())
            .orElse(null);
    }

    private String getUsername(UUID userId) {
        return userRepository.findById(userId)
            .map(User::username)
            .orElse(null);
    }
}
