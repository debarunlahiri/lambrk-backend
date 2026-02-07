package com.lambrk.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.YearMonth;

@Entity
@Table(name = "free_tier_usage", indexes = {
    @Index(name = "idx_free_tier_user_id", columnList = "user_id"),
    @Index(name = "idx_free_tier_period", columnList = "period_year, period_month"),
    @Index(name = "idx_free_tier_user_period", columnList = "user_id, period_year, period_month", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public record FreeTierUsage(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id,

    @Column(name = "user_id", nullable = false)
    Long userId,

    @Column(name = "period_year", nullable = false)
    int periodYear,

    @Column(name = "period_month", nullable = false)
    int periodMonth,

    @Column(name = "storage_bytes_used", nullable = false)
    long storageBytesUsed,

    @Column(name = "uploads_count", nullable = false)
    int uploadsCount,

    @Column(name = "bandwidth_bytes", nullable = false)
    long bandwidthBytes,

    @Column(name = "is_free_tier", nullable = false)
    boolean isFreeTier,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt
) {

    public FreeTierUsage(Long userId, int periodYear, int periodMonth, long storageBytesUsed,
                         int uploadsCount, long bandwidthBytes, boolean isFreeTier,
                         Instant createdAt, Instant updatedAt) {
        this(null, userId, periodYear, periodMonth, storageBytesUsed, uploadsCount,
             bandwidthBytes, isFreeTier, createdAt, updatedAt);
    }

    public FreeTierUsage withAddedStorage(long bytes) {
        return new FreeTierUsage(
            id, userId, periodYear, periodMonth, storageBytesUsed + bytes,
            uploadsCount, bandwidthBytes, isFreeTier, createdAt, updatedAt
        );
    }

    public FreeTierUsage withRemovedStorage(long bytes) {
        return new FreeTierUsage(
            id, userId, periodYear, periodMonth, Math.max(0, storageBytesUsed - bytes),
            uploadsCount, bandwidthBytes, isFreeTier, createdAt, updatedAt
        );
    }

    public FreeTierUsage withIncrementedUploads() {
        return new FreeTierUsage(
            id, userId, periodYear, periodMonth, storageBytesUsed,
            uploadsCount + 1, bandwidthBytes, isFreeTier, createdAt, updatedAt
        );
    }

    public FreeTierUsage withAddedBandwidth(long bytes) {
        return new FreeTierUsage(
            id, userId, periodYear, periodMonth, storageBytesUsed,
            uploadsCount, bandwidthBytes + bytes, isFreeTier, createdAt, updatedAt
        );
    }

    public static FreeTierUsage createForUser(Long userId, boolean isFreeTier) {
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
            Instant.now()
        );
    }
}
