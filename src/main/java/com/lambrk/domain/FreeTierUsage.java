package com.lambrk.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "free_tier_usage",
    indexes = {
      @Index(name = "idx_free_tier_user_id", columnList = "user_id"),
      @Index(name = "idx_free_tier_period", columnList = "period_year, period_month"),
      @Index(
          name = "idx_free_tier_user_period",
          columnList = "user_id, period_year, period_month",
          unique = true)
    })
@EntityListeners(AuditingEntityListener.class)
public class FreeTierUsage {

  @Id private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "period_year", nullable = false)
  private int periodYear;

  @Column(name = "period_month", nullable = false)
  private int periodMonth;

  @Column(name = "storage_bytes_used", nullable = false)
  private long storageBytesUsed;

  @Column(name = "uploads_count", nullable = false)
  private int uploadsCount;

  @Column(name = "bandwidth_bytes", nullable = false)
  private long bandwidthBytes;

  @Column(name = "is_free_tier", nullable = false)
  private boolean isFreeTier;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected FreeTierUsage() {}

  public FreeTierUsage(
      UUID id,
      UUID userId,
      int periodYear,
      int periodMonth,
      long storageBytesUsed,
      int uploadsCount,
      long bandwidthBytes,
      boolean isFreeTier,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.userId = userId;
    this.periodYear = periodYear;
    this.periodMonth = periodMonth;
    this.storageBytesUsed = storageBytesUsed;
    this.uploadsCount = uploadsCount;
    this.bandwidthBytes = bandwidthBytes;
    this.isFreeTier = isFreeTier;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public FreeTierUsage(
      UUID userId,
      int periodYear,
      int periodMonth,
      long storageBytesUsed,
      int uploadsCount,
      long bandwidthBytes,
      boolean isFreeTier,
      Instant createdAt,
      Instant updatedAt) {
    this(
        com.lambrk.util.UuidV7Generator.generate(),
        userId,
        periodYear,
        periodMonth,
        storageBytesUsed,
        uploadsCount,
        bandwidthBytes,
        isFreeTier,
        createdAt,
        updatedAt);
  }

  public FreeTierUsage withAddedStorage(long bytes) {
    return new FreeTierUsage(
        id,
        userId,
        periodYear,
        periodMonth,
        storageBytesUsed + bytes,
        uploadsCount,
        bandwidthBytes,
        isFreeTier,
        createdAt,
        updatedAt);
  }

  public FreeTierUsage withRemovedStorage(long bytes) {
    return new FreeTierUsage(
        id,
        userId,
        periodYear,
        periodMonth,
        Math.max(0, storageBytesUsed - bytes),
        uploadsCount,
        bandwidthBytes,
        isFreeTier,
        createdAt,
        updatedAt);
  }

  public FreeTierUsage withIncrementedUploads() {
    return new FreeTierUsage(
        id,
        userId,
        periodYear,
        periodMonth,
        storageBytesUsed,
        uploadsCount + 1,
        bandwidthBytes,
        isFreeTier,
        createdAt,
        updatedAt);
  }

  public FreeTierUsage withAddedBandwidth(long bytes) {
    return new FreeTierUsage(
        id,
        userId,
        periodYear,
        periodMonth,
        storageBytesUsed,
        uploadsCount,
        bandwidthBytes + bytes,
        isFreeTier,
        createdAt,
        updatedAt);
  }

  public static FreeTierUsage createForUser(UUID userId, boolean isFreeTier) {
    YearMonth currentMonth = YearMonth.now();
    return new FreeTierUsage(
        userId,
        currentMonth.getYear(),
        currentMonth.getMonthValue(),
        0L,
        0,
        0L,
        isFreeTier,
        Instant.now(),
        Instant.now());
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public int getPeriodYear() {
    return periodYear;
  }

  public void setPeriodYear(int periodYear) {
    this.periodYear = periodYear;
  }

  public int getPeriodMonth() {
    return periodMonth;
  }

  public void setPeriodMonth(int periodMonth) {
    this.periodMonth = periodMonth;
  }

  public long getStorageBytesUsed() {
    return storageBytesUsed;
  }

  public void setStorageBytesUsed(long storageBytesUsed) {
    this.storageBytesUsed = storageBytesUsed;
  }

  public int getUploadsCount() {
    return uploadsCount;
  }

  public void setUploadsCount(int uploadsCount) {
    this.uploadsCount = uploadsCount;
  }

  public long getBandwidthBytes() {
    return bandwidthBytes;
  }

  public void setBandwidthBytes(long bandwidthBytes) {
    this.bandwidthBytes = bandwidthBytes;
  }

  public boolean isFreeTier() {
    return isFreeTier;
  }

  public void setFreeTier(boolean freeTier) {
    this.isFreeTier = freeTier;
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
}
