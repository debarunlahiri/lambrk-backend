package com.lambrk.controller;

import com.lambrk.dto.AdminActionRequest;
import com.lambrk.dto.AdminActionResponse;
import com.lambrk.service.AdminService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/actions")
    @NewSpan("perform-admin-action")
    @Counted(value = "admin.actions.performed")
    @Timed(value = "admin.actions.duration")
    public ResponseEntity<AdminActionResponse> performAdminAction(
            @Valid @RequestBody AdminActionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long adminId = getUserId(userDetails);
        AdminActionResponse response = adminService.performAdminAction(request, adminId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/actions")
    @NewSpan("get-admin-actions")
    @Counted(value = "admin.actions.viewed")
    @Timed(value = "admin.actions.get.duration")
    public ResponseEntity<Page<AdminActionResponse>> getAdminActions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminActionResponse> actions = adminService.getAdminActions(pageable);
        return ResponseEntity.ok(actions);
    }

    @GetMapping("/actions/user/{userId}")
    @NewSpan("get-admin-actions-by-user")
    @Counted(value = "admin.actions.viewed.by-user")
    @Timed(value = "admin.actions.by-user.duration")
    public ResponseEntity<Page<AdminActionResponse>> getAdminActionsByUser(
            @PathVariable @SpanTag Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminActionResponse> actions = adminService.getAdminActionsByUser(userId, pageable);
        return ResponseEntity.ok(actions);
    }

    @GetMapping("/actions/active")
    @NewSpan("get-active-admin-actions")
    @Counted(value = "admin.actions.viewed.active")
    @Timed(value = "admin.actions.active.duration")
    public ResponseEntity<Page<AdminActionResponse>> getActiveActions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminActionResponse> actions = adminService.getActiveActions(pageable);
        return ResponseEntity.ok(actions);
    }

    @PostMapping("/ban-user/{userId}")
    @NewSpan("ban-user")
    @Counted(value = "admin.ban.user")
    @Timed(value = "admin.ban.user.duration")
    public ResponseEntity<AdminActionResponse> banUser(
            @PathVariable @SpanTag Long userId,
            @RequestParam String reason,
            @RequestParam(required = false) Integer durationDays,
            @RequestParam(defaultValue = "false") boolean permanent,
            @RequestParam(defaultValue = "true") boolean notifyUser,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        AdminActionRequest request = new AdminActionRequest(
            AdminActionRequest.AdminActionType.BAN_USER,
            userId,
            reason,
            null,
            durationDays,
            permanent,
            notifyUser
        );
        
        Long adminId = getUserId(userDetails);
        AdminActionResponse response = adminService.performAdminAction(request, adminId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/suspend-user/{userId}")
    @NewSpan("suspend-user")
    @Counted(value = "admin.suspend.user")
    @Timed(value = "admin.suspend.user.duration")
    public ResponseEntity<AdminActionResponse> suspendUser(
            @PathVariable @SpanTag Long userId,
            @RequestParam String reason,
            @RequestParam Integer durationDays,
            @RequestParam(defaultValue = "true") boolean notifyUser,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        AdminActionRequest request = new AdminActionRequest(
            AdminActionRequest.AdminActionType.SUSPEND_USER,
            userId,
            reason,
            null,
            durationDays,
            false,
            notifyUser
        );
        
        Long adminId = getUserId(userDetails);
        AdminActionResponse response = adminService.performAdminAction(request, adminId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete-post/{postId}")
    @NewSpan("delete-post-admin")
    @Counted(value = "admin.delete.post")
    @Timed(value = "admin.delete.post.duration")
    public ResponseEntity<AdminActionResponse> deletePost(
            @PathVariable @SpanTag Long postId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "true") boolean notifyUser,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        AdminActionRequest request = new AdminActionRequest(
            AdminActionRequest.AdminActionType.DELETE_POST,
            postId,
            reason,
            null,
            null,
            false,
            notifyUser
        );
        
        Long adminId = getUserId(userDetails);
        AdminActionResponse response = adminService.performAdminAction(request, adminId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete-comment/{commentId}")
    @NewSpan("delete-comment-admin")
    @Counted(value = "admin.delete.comment")
    @Timed(value = "admin.delete.comment.duration")
    public ResponseEntity<AdminActionResponse> deleteComment(
            @PathVariable @SpanTag Long commentId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "true") boolean notifyUser,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        AdminActionRequest request = new AdminActionRequest(
            AdminActionRequest.AdminActionType.DELETE_COMMENT,
            commentId,
            reason,
            null,
            null,
            false,
            notifyUser
        );
        
        Long adminId = getUserId(userDetails);
        AdminActionResponse response = adminService.performAdminAction(request, adminId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/lock-post/{postId}")
    @NewSpan("lock-post-admin")
    @Counted(value = "admin.lock.post")
    @Timed(value = "admin.lock.post.duration")
    public ResponseEntity<AdminActionResponse> lockPost(
            @PathVariable @SpanTag Long postId,
            @RequestParam String reason,
            @RequestParam(required = false) Integer durationDays,
            @RequestParam(defaultValue = "false") boolean permanent,
            @RequestParam(defaultValue = "true") boolean notifyUser,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        AdminActionRequest request = new AdminActionRequest(
            AdminActionRequest.AdminActionType.LOCK_POST,
            postId,
            reason,
            null,
            durationDays,
            permanent,
            notifyUser
        );
        
        Long adminId = getUserId(userDetails);
        AdminActionResponse response = adminService.performAdminAction(request, adminId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/quarantine-post/{postId}")
    @NewSpan("quarantine-post")
    @Counted(value = "admin.quarantine.post")
    @Timed(value = "admin.quarantine.post.duration")
    public ResponseEntity<AdminActionResponse> quarantinePost(
            @PathVariable @SpanTag Long postId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "true") boolean notifyUser,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        AdminActionRequest request = new AdminActionRequest(
            AdminActionRequest.AdminActionType.QUARANTINE_POST,
            postId,
            reason,
            null,
            null,
            false,
            notifyUser
        );
        
        Long adminId = getUserId(userDetails);
        AdminActionResponse response = adminService.performAdminAction(request, adminId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove-moderator/{userId}")
    @NewSpan("remove-moderator")
    @Counted(value = "admin.remove.moderator")
    @Timed(value = "admin.remove.moderator.duration")
    public ResponseEntity<AdminActionResponse> removeModerator(
            @PathVariable @SpanTag Long userId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "true") boolean notifyUser,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        AdminActionRequest request = new AdminActionRequest(
            AdminActionRequest.AdminActionType.REMOVE_MODERATOR,
            userId,
            reason,
            null,
            null,
            false,
            notifyUser
        );
        
        Long adminId = getUserId(userDetails);
        AdminActionResponse response = adminService.performAdminAction(request, adminId);
        return ResponseEntity.ok(response);
    }

    private Long getUserId(UserDetails userDetails) {
        // In a real implementation, extract user ID from UserDetails
        return 1L; // Placeholder for admin user
    }
}
