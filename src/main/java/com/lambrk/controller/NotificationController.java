package com.lambrk.controller;

import com.lambrk.dto.NotificationRequest;
import com.lambrk.dto.NotificationResponse;
import com.lambrk.service.NotificationService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @NewSpan("create-notification")
    @Counted(value = "notifications.created")
    @Timed(value = "notifications.create.duration")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @NewSpan("get-notifications")
    @Counted(value = "notifications.viewed")
    @Timed(value = "notifications.get.duration")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    @NewSpan("get-unread-notifications")
    @Counted(value = "notifications.unread.viewed")
    @Timed(value = "notifications.unread.duration")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}/read")
    @NewSpan("mark-notification-read")
    @Counted(value = "notifications.read")
    @Timed(value = "notifications.read.duration")
    public ResponseEntity<Void> markNotificationAsRead(
            @PathVariable @SpanTag Long notificationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        notificationService.markNotificationAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @NewSpan("mark-all-notifications-read")
    @Counted(value = "notifications.read.all")
    @Timed(value = "notifications.read.all.duration")
    public ResponseEntity<Void> markAllNotificationsAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        notificationService.markAllNotificationsAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    @NewSpan("delete-notification")
    @Counted(value = "notifications.deleted")
    @Timed(value = "notifications.delete.duration")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable @SpanTag Long notificationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @NewSpan("delete-all-notifications")
    @Counted(value = "notifications.deleted.all")
    @Timed(value = "notifications.delete.all.duration")
    public ResponseEntity<Void> deleteAllNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        notificationService.deleteAllNotifications(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/unread")
    @NewSpan("count-unread-notifications")
    @Counted(value = "notifications.count.unread")
    @Timed(value = "notifications.count.unread.duration")
    public ResponseEntity<Long> getUnreadNotificationCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        Pageable pageable = PageRequest.of(0, 1);
        Page<NotificationResponse> unreadNotifications = notificationService.getUnreadNotifications(userId, pageable);
        long count = unreadNotifications.getTotalElements();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/type/{type}")
    @NewSpan("get-notifications-by-type")
    @Counted(value = "notifications.viewed.by-type")
    @Timed(value = "notifications.by-type.duration")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsByType(
            @PathVariable @SpanTag NotificationRequest.NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // This would need to be implemented in NotificationService
        // For now, return empty page
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> notifications = Page.empty(pageable);
        return ResponseEntity.ok(notifications);
    }

    private Long getUserId(UserDetails userDetails) {
        // In a real implementation, extract user ID from UserDetails
        return 1L; // Placeholder
    }
}
