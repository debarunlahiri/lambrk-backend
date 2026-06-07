package com.lambrk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "admin_actions",
    indexes = {
      @Index(name = "idx_admin_action_type", columnList = "type"),
      @Index(name = "idx_admin_action_target", columnList = "target_id"),
      @Index(name = "idx_admin_action_performed_by", columnList = "performed_by"),
      @Index(name = "idx_admin_action_created_at", columnList = "created_at"),
      @Index(name = "idx_admin_action_is_active", columnList = "is_active")
    })
@EntityListeners(AuditingEntityListener.class)
public class AdminAction {

  @Id private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private AdminActionType type;

  @Column(name = "target_id", nullable = false)
  private UUID targetId;

  @Column(name = "target_type", nullable = false, length = 50)
  private String targetType;

  @NotBlank(message = "Reason is required")
  @Size(max = 1000, message = "Reason must be less than 1000 characters")
  @Column(columnDefinition = "TEXT", nullable = false)
  private String reason;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "performed_by", nullable = false)
  private UUID performedBy;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @Column(name = "result", length = 500)
  private String result;

  protected AdminAction() {}

  public AdminAction(
      UUID id,
      AdminActionType type,
      UUID targetId,
      String targetType,
      String reason,
      String notes,
      UUID performedBy,
      Instant createdAt,
      Instant expiresAt,
      boolean isActive,
      String result) {
    this.id = id;
    this.type = type;
    this.targetId = targetId;
    this.targetType = targetType;
    this.reason = reason;
    this.notes = notes;
    this.performedBy = performedBy;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
    this.isActive = isActive;
    this.result = result;
  }

  public AdminAction(
      AdminActionType type,
      UUID targetId,
      String targetType,
      String reason,
      String notes,
      UUID performedBy,
      Instant createdAt,
      Instant expiresAt,
      boolean isActive,
      String result) {
    this(
        com.lambrk.util.UuidV7Generator.generate(),
        type,
        targetId,
        targetType,
        reason,
        notes,
        performedBy,
        createdAt,
        expiresAt,
        isActive,
        result);
  }

  public enum AdminActionType {
    BAN_USER,
    SUSPEND_USER,
    DELETE_POST,
    DELETE_COMMENT,
    LOCK_POST,
    LOCK_COMMENT,
    REMOVE_MODERATOR,
    ADD_MODERATOR,
    BAN_COMMUNITY,
    QUARANTINE_POST,
    QUARANTINE_COMMENT
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public AdminActionType getType() {
    return type;
  }

  public void setType(AdminActionType type) {
    this.type = type;
  }

  public UUID getTargetId() {
    return targetId;
  }

  public void setTargetId(UUID targetId) {
    this.targetId = targetId;
  }

  public String getTargetType() {
    return targetType;
  }

  public void setTargetType(String targetType) {
    this.targetType = targetType;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public UUID getPerformedBy() {
    return performedBy;
  }

  public void setPerformedBy(UUID performedBy) {
    this.performedBy = performedBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    this.isActive = active;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }
}
