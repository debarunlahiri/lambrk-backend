package com.lambrk.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "user_friendships",
    indexes = {
      @Index(name = "idx_user_friendship_user_one", columnList = "user_one_id"),
      @Index(name = "idx_user_friendship_user_two", columnList = "user_two_id"),
      @Index(name = "idx_user_friendship_requester", columnList = "requester_id"),
      @Index(name = "idx_user_friendship_addressee", columnList = "addressee_id"),
      @Index(name = "idx_user_friendship_status", columnList = "status"),
      @Index(
          name = "idx_user_friendship_pair",
          columnList = "user_one_id, user_two_id",
          unique = true)
    })
@EntityListeners(AuditingEntityListener.class)
public class UserFriendship {

  public enum FriendshipStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    CANCELLED,
    REMOVED,
    BLOCKED
  }

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_one_id", nullable = false)
  private User userOne;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_two_id", nullable = false)
  private User userTwo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "requester_id", nullable = false)
  private User requester;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "addressee_id", nullable = false)
  private User addressee;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_action_user_id", nullable = false)
  private User lastActionUser;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private FriendshipStatus status = FriendshipStatus.PENDING;

  @Column(length = 50)
  private String source;

  @Column(name = "request_message", length = 280)
  private String requestMessage;

  @Column(name = "accepted_at")
  private Instant acceptedAt;

  @Column(name = "responded_at")
  private Instant respondedAt;

  @Column(name = "removed_at")
  private Instant removedAt;

  @Column(name = "blocked_at")
  private Instant blockedAt;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected UserFriendship() {}

  public UserFriendship(
      UUID id,
      User userOne,
      User userTwo,
      User requester,
      User addressee,
      String source,
      String requestMessage) {
    this.id = id;
    this.userOne = userOne;
    this.userTwo = userTwo;
    this.requester = requester;
    this.addressee = addressee;
    this.lastActionUser = requester;
    this.source = source;
    this.requestMessage = requestMessage;
    this.status = FriendshipStatus.PENDING;
  }

  public UUID getId() {
    return id;
  }

  public User getUserOne() {
    return userOne;
  }

  public User getUserTwo() {
    return userTwo;
  }

  public User getRequester() {
    return requester;
  }

  public User getAddressee() {
    return addressee;
  }

  public User getLastActionUser() {
    return lastActionUser;
  }

  public FriendshipStatus getStatus() {
    return status;
  }

  public String getSource() {
    return source;
  }

  public String getRequestMessage() {
    return requestMessage;
  }

  public Instant getAcceptedAt() {
    return acceptedAt;
  }

  public Instant getRespondedAt() {
    return respondedAt;
  }

  public Instant getRemovedAt() {
    return removedAt;
  }

  public Instant getBlockedAt() {
    return blockedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public User getOtherUser(UUID currentUserId) {
    return userOne.getId().equals(currentUserId) ? userTwo : userOne;
  }

  public void requestAgain(User requester, User addressee, String source, String requestMessage) {
    this.requester = requester;
    this.addressee = addressee;
    this.lastActionUser = requester;
    this.source = source;
    this.requestMessage = requestMessage;
    this.status = FriendshipStatus.PENDING;
    this.acceptedAt = null;
    this.respondedAt = null;
    this.removedAt = null;
    this.blockedAt = null;
  }

  public void accept(User actionUser) {
    Instant now = Instant.now();
    this.status = FriendshipStatus.ACCEPTED;
    this.lastActionUser = actionUser;
    this.acceptedAt = now;
    this.respondedAt = now;
    this.removedAt = null;
  }

  public void decline(User actionUser) {
    this.status = FriendshipStatus.DECLINED;
    this.lastActionUser = actionUser;
    this.respondedAt = Instant.now();
  }

  public void cancel(User actionUser) {
    this.status = FriendshipStatus.CANCELLED;
    this.lastActionUser = actionUser;
    this.respondedAt = Instant.now();
  }

  public void remove(User actionUser) {
    this.status = FriendshipStatus.REMOVED;
    this.lastActionUser = actionUser;
    this.removedAt = Instant.now();
  }
}
