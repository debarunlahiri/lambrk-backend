package com.lambrk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "notifications",
    indexes = {
      @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
      @Index(name = "idx_notification_type", columnList = "type"),
      @Index(name = "idx_notification_created_at", columnList = "created_at"),
      @Index(name = "idx_notification_is_read", columnList = "is_read")
    })
@EntityListeners(AuditingEntityListener.class)
public class Notification {

  @Id private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private NotificationType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipient_id", nullable = false)
  private User recipient;

  @NotBlank(message = "Title is required")
  @Size(max = 500, message = "Title must be less than 500 characters")
  @Column(nullable = false, length = 500)
  private String title;

  @NotBlank(message = "Message is required")
  @Size(max = 2000, message = "Message must be less than 2000 characters")
  @Column(columnDefinition = "TEXT", nullable = false)
  private String message;

  @Column(name = "related_post_id")
  private UUID relatedPostId;

  @Column(name = "related_comment_id")
  private UUID relatedCommentId;

  @Column(name = "related_user_id")
  private UUID relatedUserId;

  @Column(name = "action_url", length = 500)
  private String actionUrl;

  @Column(name = "action_text", length = 100)
  private String actionText;

  @Column(name = "is_read", nullable = false)
  private boolean isRead;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "read_at")
  private Instant readAt;

  protected Notification() {}

  public Notification(
      UUID id,
      NotificationType type,
      User recipient,
      String title,
      String message,
      UUID relatedPostId,
      UUID relatedCommentId,
      UUID relatedUserId,
      String actionUrl,
      String actionText,
      boolean isRead,
      Instant createdAt,
      Instant updatedAt,
      Instant readAt) {
    this.id = id;
    this.type = type;
    this.recipient = recipient;
    this.title = title;
    this.message = message;
    this.relatedPostId = relatedPostId;
    this.relatedCommentId = relatedCommentId;
    this.relatedUserId = relatedUserId;
    this.actionUrl = actionUrl;
    this.actionText = actionText;
    this.isRead = isRead;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.readAt = readAt;
  }

  public Notification(
      NotificationType type,
      User recipient,
      String title,
      String message,
      UUID relatedPostId,
      UUID relatedCommentId,
      UUID relatedUserId,
      String actionUrl,
      String actionText,
      boolean isRead,
      Instant createdAt,
      Instant updatedAt,
      Instant readAt) {
    this(
        com.lambrk.util.UuidV7Generator.generate(),
        type,
        recipient,
        title,
        message,
        relatedPostId,
        relatedCommentId,
        relatedUserId,
        actionUrl,
        actionText,
        isRead,
        createdAt,
        updatedAt,
        readAt);
  }

  public enum NotificationType {
    COMMENT_REPLY,
    POST_LIKE,
    COMMENT_LIKE,
    POST_MENTION,
    COMMENT_MENTION,
    COMMUNITY_INVITE,
    MODERATOR_ACTION,
    SYSTEM_ANNOUNCEMENT,
    CONTENT_MODERATION,
    USER_FOLLOW,
    FRIEND_REQUEST,
    FRIEND_REQUEST_ACCEPTED
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public NotificationType getType() {
    return type;
  }

  public void setType(NotificationType type) {
    this.type = type;
  }

  public User getRecipient() {
    return recipient;
  }

  public void setRecipient(User recipient) {
    this.recipient = recipient;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public UUID getRelatedPostId() {
    return relatedPostId;
  }

  public void setRelatedPostId(UUID relatedPostId) {
    this.relatedPostId = relatedPostId;
  }

  public UUID getRelatedCommentId() {
    return relatedCommentId;
  }

  public void setRelatedCommentId(UUID relatedCommentId) {
    this.relatedCommentId = relatedCommentId;
  }

  public UUID getRelatedUserId() {
    return relatedUserId;
  }

  public void setRelatedUserId(UUID relatedUserId) {
    this.relatedUserId = relatedUserId;
  }

  public String getActionUrl() {
    return actionUrl;
  }

  public void setActionUrl(String actionUrl) {
    this.actionUrl = actionUrl;
  }

  public String getActionText() {
    return actionText;
  }

  public void setActionText(String actionText) {
    this.actionText = actionText;
  }

  public boolean isRead() {
    return isRead;
  }

  public void setRead(boolean read) {
    this.isRead = read;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Instant getReadAt() {
    return readAt;
  }

  public void setReadAt(Instant readAt) {
    this.readAt = readAt;
  }
}
