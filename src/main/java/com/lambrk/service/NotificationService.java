package com.lambrk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambrk.domain.Comment;
import com.lambrk.domain.Notification;
import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.dto.NotificationRequest;
import com.lambrk.dto.NotificationResponse;
import com.lambrk.repository.CommentRepository;
import com.lambrk.repository.NotificationRepository;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.UserRepository;
import com.lambrk.util.UuidV7Generator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final KafkaTemplate<String, byte[]> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final CustomMetrics customMetrics;

  public NotificationService(
      NotificationRepository notificationRepository,
      UserRepository userRepository,
      CommentRepository commentRepository,
      PostRepository postRepository,
      KafkaTemplate<String, byte[]> kafkaTemplate,
      ObjectMapper objectMapper,
      CustomMetrics customMetrics) {
    this.notificationRepository = notificationRepository;
    this.userRepository = userRepository;
    this.commentRepository = commentRepository;
    this.postRepository = postRepository;
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.customMetrics = customMetrics;
  }

  @CacheEvict(value = "notifications", allEntries = true)
  @CircuitBreaker(name = "userService")
  @Retry(name = "userService")
  public NotificationResponse createNotification(NotificationRequest request) {
    User recipient =
        userRepository
            .findById(request.recipientId())
            .orElseThrow(() -> new RuntimeException("User not found: " + request.recipientId()));

    Notification notification =
        new Notification(
            UuidV7Generator.generate(),
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
            null);

    Notification saved = notificationRepository.save(notification);

    // Send real-time notification via WebSocket/Kafka
    sendRealTimeNotification(saved);

    // Update metrics
    customMetrics.recordNotificationCreated(request.type().name());

    return convertToResponse(saved);
  }

  @Cacheable(value = "notifications", key = "#userId")
  public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
    Page<Notification> notifications =
        notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
    return notifications.map(this::convertToResponse);
  }

  @Cacheable(value = "notifications", key = "#userId + '-unread'")
  public Page<NotificationResponse> getUnreadNotifications(UUID userId, Pageable pageable) {
    Page<Notification> notifications =
        notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(
            userId, false, pageable);
    return notifications.map(this::convertToResponse);
  }

  public long getUnreadNotificationCount(UUID userId) {
    return notificationRepository.countUnreadNotifications(userId);
  }

  @CacheEvict(value = "notifications", allEntries = true)
  public void markNotificationAsRead(UUID notificationId, UUID userId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

    if (!notification.getRecipient().getId().equals(userId)) {
      throw new RuntimeException("Notification does not belong to user: " + userId);
    }

    Notification updated =
        new Notification(
            notification.getId(),
            notification.getType(),
            notification.getRecipient(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getRelatedPostId(),
            notification.getRelatedCommentId(),
            notification.getRelatedUserId(),
            notification.getActionUrl(),
            notification.getActionText(),
            true,
            notification.getCreatedAt(),
            Instant.now(),
            Instant.now());

    notificationRepository.save(updated);
  }

  @CacheEvict(value = "notifications", key = "#userId")
  public void markAllNotificationsAsRead(UUID userId) {
    List<Notification> unreadNotifications =
        notificationRepository.findByRecipientIdAndIsRead(userId, false);

    for (Notification notification : unreadNotifications) {
      Notification updated =
          new Notification(
              notification.getId(),
              notification.getType(),
              notification.getRecipient(),
              notification.getTitle(),
              notification.getMessage(),
              notification.getRelatedPostId(),
              notification.getRelatedCommentId(),
              notification.getRelatedUserId(),
              notification.getActionUrl(),
              notification.getActionText(),
              true,
              notification.getCreatedAt(),
              Instant.now(),
              Instant.now());
      notificationRepository.save(updated);
    }
  }

  @CacheEvict(value = "notifications", key = "#userId")
  public void deleteNotification(UUID notificationId, UUID userId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

    if (!notification.getRecipient().getId().equals(userId)) {
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
      Comment comment =
          commentRepository
              .findById(commentId)
              .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));
      Post post =
          postRepository
              .findById(postId)
              .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
      User author =
          userRepository
              .findById(authorId)
              .orElseThrow(() -> new RuntimeException("Author not found: " + authorId));

      // Don't notify if user is replying to their own comment
      if (comment.getParent() != null && comment.getParent().getAuthor().getId().equals(authorId)) {
        return;
      }

      UUID recipientId =
          comment.getParent() != null
              ? comment.getParent().getAuthor().getId()
              : post.getAuthor().getId();

      NotificationRequest notification =
          new NotificationRequest(
              NotificationRequest.NotificationType.COMMENT_REPLY,
              recipientId,
              "New reply to your comment",
              String.format(
                  "%s replied: \"%s\"",
                  author.getUsername(),
                  comment.getContent().length() > 100
                      ? comment.getContent().substring(0, 100) + "..."
                      : comment.getContent()),
              postId,
              commentId,
              authorId,
              "/posts/" + postId + "#comment-" + commentId,
              "View reply",
              false);

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
      Post post =
          postRepository
              .findById(postId)
              .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
      User voter =
          userRepository
              .findById(voterId)
              .orElseThrow(() -> new RuntimeException("Voter not found: " + voterId));

      NotificationRequest notification =
          new NotificationRequest(
              NotificationRequest.NotificationType.POST_LIKE,
              authorId,
              "Your post received a like",
              String.format(
                  "%s liked your post \"%s\"",
                  voter.getUsername(), post.getTitle() != null ? post.getTitle() : "Untitled"),
              postId,
              null,
              voterId,
              "/posts/" + postId,
              "View post",
              false);

      createNotification(notification);

    } catch (Exception e) {
      System.err.println("Failed to create like notification: " + e.getMessage());
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createFollowNotification(UUID followerId, UUID followedUserId) {
    if (followerId.equals(followedUserId)) {
      return;
    }

    try {
      User follower =
          userRepository
              .findById(followerId)
              .orElseThrow(() -> new RuntimeException("Follower not found: " + followerId));

      NotificationRequest notification =
          new NotificationRequest(
              NotificationRequest.NotificationType.USER_FOLLOW,
              followedUserId,
              "New follower",
              String.format("%s started following you", follower.getUsername()),
              null,
              null,
              followerId,
              "/users/" + follower.getUsername(),
              "View profile",
              false);

      createNotification(notification);
    } catch (Exception e) {
      System.err.println("Failed to create follow notification: " + e.getMessage());
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createFriendRequestNotification(UUID requesterId, UUID addresseeId) {
    if (requesterId.equals(addresseeId)) {
      return;
    }

    try {
      User requester =
          userRepository
              .findById(requesterId)
              .orElseThrow(() -> new RuntimeException("Requester not found: " + requesterId));

      NotificationRequest notification =
          new NotificationRequest(
              NotificationRequest.NotificationType.FRIEND_REQUEST,
              addresseeId,
              "New friend request",
              String.format("%s sent you a friend request", requester.getUsername()),
              null,
              null,
              requesterId,
              "/users/" + requester.getUsername(),
              "View request",
              false);

      createNotification(notification);
    } catch (Exception e) {
      System.err.println("Failed to create friend request notification: " + e.getMessage());
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createFriendRequestAcceptedNotification(UUID acceptedByUserId, UUID requesterId) {
    if (acceptedByUserId.equals(requesterId)) {
      return;
    }

    try {
      User acceptedBy =
          userRepository
              .findById(acceptedByUserId)
              .orElseThrow(() -> new RuntimeException("Friend not found: " + acceptedByUserId));

      NotificationRequest notification =
          new NotificationRequest(
              NotificationRequest.NotificationType.FRIEND_REQUEST_ACCEPTED,
              requesterId,
              "Friend request accepted",
              String.format("%s accepted your friend request", acceptedBy.getUsername()),
              null,
              null,
              acceptedByUserId,
              "/users/" + acceptedBy.getUsername(),
              "View profile",
              false);

      createNotification(notification);
    } catch (Exception e) {
      System.err.println(
          "Failed to create friend request accepted notification: " + e.getMessage());
    }
  }

  public void createMentionNotification(
      String content, UUID postId, UUID commentId, UUID mentionedUserId, UUID authorId) {
    if (mentionedUserId.equals(authorId)) {
      return; // Don't notify for self-mentions
    }

    try {
      User author =
          userRepository
              .findById(authorId)
              .orElseThrow(() -> new RuntimeException("Author not found: " + authorId));

      NotificationRequest notification =
          new NotificationRequest(
              NotificationRequest.NotificationType.POST_MENTION,
              mentionedUserId,
              "You were mentioned in a post",
              String.format(
                  "%s mentioned you in \"%s\"",
                  author.getUsername(),
                  content.length() > 100 ? content.substring(0, 100) + "..." : content),
              postId,
              commentId,
              authorId,
              commentId != null ? "/posts/" + postId + "#comment-" + commentId : "/posts/" + postId,
              "View mention",
              false);

      createNotification(notification);

    } catch (Exception e) {
      System.err.println("Failed to create mention notification: " + e.getMessage());
    }
  }

  private void sendRealTimeNotification(Notification notification) {
    try {
      byte[] payload = toKafkaPayload(notification);
      String recipientId = notification.getRecipient().getId().toString();

      // Send to WebSocket topic
      kafkaTemplate.send("notifications", notification.getId().toString(), payload);

      // Could also send to specific user topic
      kafkaTemplate.send(
          "user-" + recipientId + "-notifications", notification.getId().toString(), payload);

    } catch (Exception e) {
      System.err.println("Failed to send real-time notification: " + e.getMessage());
    }
  }

  private byte[] toKafkaPayload(Notification notification) throws JsonProcessingException {
    return objectMapper
        .writeValueAsString(
            new NotificationRealtimeEvent(
                notification.getId(),
                notification.getType().name(),
                notification.getRecipient().getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRelatedPostId(),
                notification.getRelatedCommentId(),
                notification.getRelatedUserId(),
                notification.getActionUrl(),
                notification.getActionText(),
                notification.isRead(),
                notification.getCreatedAt()))
        .getBytes(StandardCharsets.UTF_8);
  }

  private NotificationResponse convertToResponse(Notification notification) {
    return new NotificationResponse(
        notification.getId(),
        notification.getType(),
        notification.getRecipient().getId(),
        notification.getTitle(),
        notification.getMessage(),
        notification.getRelatedPostId(),
        getPostTitle(notification.getRelatedPostId()),
        notification.getRelatedCommentId(),
        getCommentPreview(notification.getRelatedCommentId()),
        notification.getRelatedUserId(),
        getUsername(notification.getRelatedUserId()),
        notification.getActionUrl(),
        notification.getActionText(),
        notification.isRead(),
        notification.getCreatedAt(),
        notification.getReadAt());
  }

  private String getPostTitle(UUID postId) {
    if (postId == null) {
      return null;
    }
    return postRepository.findById(postId).map(Post::getTitle).orElse(null);
  }

  private String getCommentPreview(UUID commentId) {
    if (commentId == null) {
      return null;
    }
    return commentRepository
        .findById(commentId)
        .map(
            c ->
                c.getContent().length() > 100
                    ? c.getContent().substring(0, 100) + "..."
                    : c.getContent())
        .orElse(null);
  }

  private String getUsername(UUID userId) {
    if (userId == null) {
      return null;
    }
    return userRepository.findById(userId).map(User::getUsername).orElse(null);
  }

  private record NotificationRealtimeEvent(
      UUID id,
      String type,
      UUID recipientId,
      String title,
      String message,
      UUID relatedPostId,
      UUID relatedCommentId,
      UUID relatedUserId,
      String actionUrl,
      String actionText,
      boolean read,
      Instant createdAt) {}
}
