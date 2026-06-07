package com.lambrk.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "user_follows",
    indexes = {
      @Index(name = "idx_user_follow_follower", columnList = "follower_id"),
      @Index(name = "idx_user_follow_following", columnList = "following_id"),
      @Index(name = "idx_user_follow_status", columnList = "status"),
      @Index(name = "idx_user_follow_created_at", columnList = "created_at"),
      @Index(name = "idx_user_follow_pair", columnList = "follower_id, following_id", unique = true)
    })
@EntityListeners(AuditingEntityListener.class)
public class UserFollow {

  public enum FollowStatus {
    ACTIVE,
    REMOVED
  }

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "follower_id", nullable = false)
  private User follower;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "following_id", nullable = false)
  private User following;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private FollowStatus status = FollowStatus.ACTIVE;

  @Column(name = "notification_enabled", nullable = false)
  private boolean notificationEnabled = true;

  @Column(length = 50)
  private String source;

  @Column(name = "last_interaction_at")
  private Instant lastInteractionAt;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "removed_at")
  private Instant removedAt;

  protected UserFollow() {}

  public UserFollow(UUID id, User follower, User following, String source) {
    this.id = id;
    this.follower = follower;
    this.following = following;
    this.source = source;
    this.status = FollowStatus.ACTIVE;
  }

  public UUID getId() {
    return id;
  }

  public User getFollower() {
    return follower;
  }

  public User getFollowing() {
    return following;
  }

  public FollowStatus getStatus() {
    return status;
  }

  public boolean isNotificationEnabled() {
    return notificationEnabled;
  }

  public String getSource() {
    return source;
  }

  public Instant getLastInteractionAt() {
    return lastInteractionAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getRemovedAt() {
    return removedAt;
  }

  public void activate(String source) {
    this.status = FollowStatus.ACTIVE;
    this.source = source;
    this.removedAt = null;
    this.lastInteractionAt = Instant.now();
  }

  public void remove() {
    this.status = FollowStatus.REMOVED;
    this.removedAt = Instant.now();
    this.lastInteractionAt = Instant.now();
  }
}
