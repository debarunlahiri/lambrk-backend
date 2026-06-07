package com.lambrk.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "user_community_moderators")
@EntityListeners(AuditingEntityListener.class)
public class UserCommunityModerator {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "community_id", nullable = false)
  private Community community;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ModeratorRole role = ModeratorRole.MODERATOR;

  @Column(name = "assigned_by")
  private UUID assignedBy;

  @CreatedDate
  @Column(name = "assigned_at", nullable = false, updatable = false)
  private Instant assignedAt;

  @Column(name = "removed_at")
  private Instant removedAt;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  public UserCommunityModerator() {
    this.id = com.lambrk.util.UuidV7Generator.generate();
  }

  public UserCommunityModerator(User user, Community community) {
    this();
    this.user = user;
    this.community = community;
    this.role = ModeratorRole.MODERATOR;
    this.assignedAt = Instant.now();
    this.isActive = true;
  }

  public enum ModeratorRole {
    MODERATOR,
    ADMIN,
    OWNER
  }

  // Getters and setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Community getCommunity() {
    return community;
  }

  public void setCommunity(Community community) {
    this.community = community;
  }

  public ModeratorRole getRole() {
    return role;
  }

  public void setRole(ModeratorRole role) {
    this.role = role;
  }

  public UUID getAssignedBy() {
    return assignedBy;
  }

  public void setAssignedBy(UUID assignedBy) {
    this.assignedBy = assignedBy;
  }

  public Instant getAssignedAt() {
    return assignedAt;
  }

  public void setAssignedAt(Instant assignedAt) {
    this.assignedAt = assignedAt;
  }

  public Instant getRemovedAt() {
    return removedAt;
  }

  public void setRemovedAt(Instant removedAt) {
    this.removedAt = removedAt;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }
}
