package com.lambrk.repository;

import com.lambrk.domain.AdminAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AdminActionRepository extends JpaRepository<AdminAction, Long> {

    @Query("SELECT a FROM AdminAction a ORDER BY a.createdAt DESC")
    Page<AdminAction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT a FROM AdminAction a WHERE a.performedBy = :adminId ORDER BY a.createdAt DESC")
    Page<AdminAction> findByPerformedByOrderByCreatedAtDesc(@Param("adminId") Long adminId, Pageable pageable);

    @Query("SELECT a FROM AdminAction a WHERE a.targetId = :targetId AND a.targetType = :targetType ORDER BY a.createdAt DESC")
    Page<AdminAction> findByTargetIdAndTargetTypeOrderByCreatedAtDesc(@Param("targetId") Long targetId, @Param("targetType") String targetType, Pageable pageable);

    @Query("SELECT a FROM AdminAction a WHERE a.isActive = true ORDER BY a.createdAt DESC")
    Page<AdminAction> findByIsActiveOrderByCreatedAtDesc(@Param("isActive") boolean isActive, Pageable pageable);

    @Query("SELECT a FROM AdminAction a WHERE a.type = :type ORDER BY a.createdAt DESC")
    Page<AdminAction> findByTypeOrderByCreatedAtDesc(@Param("type") AdminAction.AdminActionType type, Pageable pageable);

    @Query("SELECT a FROM AdminAction a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AdminAction> findActionsSince(@Param("since") Instant since);

    @Query("SELECT a FROM AdminAction a WHERE a.expiresAt IS NOT NULL AND a.expiresAt <= :before")
    List<AdminAction> findExpiredActions(@Param("before") Instant before);

    @Query("SELECT COUNT(a) FROM AdminAction a WHERE a.performedBy = :adminId AND a.createdAt >= :since")
    long countActionsByAdminSince(@Param("adminId") Long adminId, @Param("since") Instant since);

    @Query("SELECT COUNT(a) FROM AdminAction a WHERE a.type = :type AND a.createdAt >= :since")
    long countActionsByTypeSince(@Param("type") AdminAction.AdminActionType type, @Param("since") Instant since);

    @Query("SELECT a.type, COUNT(a) FROM AdminAction a WHERE a.createdAt >= :since GROUP BY a.type")
    List<Object[]> getActionTypeStatsSince(@Param("since") Instant since);

    @Query("SELECT a.performedBy, COUNT(a) FROM AdminAction a WHERE a.createdAt >= :since GROUP BY a.performedBy ORDER BY COUNT(a) DESC")
    List<Object[]> getTopAdminsSince(@Param("since") Instant since);
}
