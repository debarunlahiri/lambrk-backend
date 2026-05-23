package com.lambrk.repository;

import com.lambrk.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId AND n.isRead = :isRead ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(@Param("userId") UUID userId, @Param("isRead") boolean isRead, Pageable pageable);

    List<Notification> findByRecipientIdAndIsRead(UUID recipientId, boolean isRead);

    List<Notification> findByRecipientId(UUID recipientId);

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId AND n.type = :type ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(@Param("userId") UUID userId, @Param("type") Notification.NotificationType type, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :userId AND n.isRead = false")
    long countUnreadNotifications(@Param("userId") UUID userId);

    @Query("SELECT n FROM Notification n WHERE n.createdAt < :before ORDER BY n.createdAt DESC")
    List<Notification> findOldNotifications(@Param("before") Instant before);

    @Query("DELETE FROM Notification n WHERE n.createdAt < :before")
    void deleteOldNotifications(@Param("before") Instant before);
}
