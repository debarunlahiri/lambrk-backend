package com.lambrk.service;

import com.lambrk.domain.AdminAction;
import com.lambrk.domain.User;
import com.lambrk.domain.Post;
import com.lambrk.domain.Comment;
import com.lambrk.domain.Subreddit;
import com.lambrk.dto.AdminActionRequest;
import com.lambrk.dto.AdminActionResponse;
import com.lambrk.repository.AdminActionRepository;
import com.lambrk.repository.UserRepository;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.CommentRepository;
import com.lambrk.repository.SubredditRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

@Service
@Transactional
public class AdminService {

    private final AdminActionRepository adminActionRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final SubredditRepository subredditRepository;
    private final NotificationService notificationService;
    private final KafkaTemplate kafkaTemplate;
    private final CustomMetrics customMetrics;

    public AdminService(AdminActionRepository adminActionRepository,
                         UserRepository userRepository,
                         PostRepository postRepository,
                         CommentRepository commentRepository,
                         SubredditRepository subredditRepository,
                         NotificationService notificationService,
                         KafkaTemplate kafkaTemplate,
                         CustomMetrics customMetrics) {
        this.adminActionRepository = adminActionRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.subredditRepository = subredditRepository;
        this.notificationService = notificationService;
        this.kafkaTemplate = kafkaTemplate;
        this.customMetrics = customMetrics;
    }

    @CacheEvict(value = {"users", "posts", "comments", "subreddits"}, allEntries = true)
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public AdminActionResponse performAdminAction(AdminActionRequest request, Long adminId) {
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new RuntimeException("Admin not found: " + adminId));

        AdminAction action = executeAction(request, admin);
        AdminAction saved = adminActionRepository.save(action);

        // Send notifications if requested
        if (request.notifyUser()) {
            sendActionNotification(saved);
        }

        // Log to audit trail
        sendAuditEvent(saved);

        customMetrics.recordAdminAction(request.action().name());

        return AdminActionResponse.from(saved);
    }

    private AdminAction executeAction(AdminActionRequest request, User admin) {
        Instant now = Instant.now();
        Instant expiresAt = request.permanent() ? null : now.plusSeconds(request.durationDays() * 86400L);

        return switch (request.action()) {
            case BAN_USER -> banUser(request.targetId(), request.reason(), expiresAt, admin, now);
            case SUSPEND_USER -> suspendUser(request.targetId(), request.reason(), expiresAt, admin, now);
            case DELETE_POST -> deletePost(request.targetId(), request.reason(), admin, now);
            case DELETE_COMMENT -> deleteComment(request.targetId(), request.reason(), admin, now);
            case LOCK_POST -> lockPost(request.targetId(), request.reason(), expiresAt, admin, now);
            case LOCK_COMMENT -> lockComment(request.targetId(), request.reason(), expiresAt, admin, now);
            case REMOVE_MODERATOR -> removeModerator(request.targetId(), request.reason(), admin, now);
            case ADD_MODERATOR -> addModerator(request.targetId(), request.reason(), admin, now);
            case BAN_SUBREDDIT -> banSubreddit(request.targetId(), request.reason(), expiresAt, admin, now);
            case QUARANTINE_POST -> quarantinePost(request.targetId(), request.reason(), admin, now);
            case QUARANTINE_COMMENT -> quarantineComment(request.targetId(), request.reason(), admin, now);
        };
    }

    private AdminAction banUser(Long userId, String reason, Instant expiresAt, User admin, Instant now) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        User updated = new User(
            user.id(), user.username(), user.email(), user.password(),
            user.displayName(), user.bio(), user.avatarUrl(),
            false, user.isVerified(), user.karma(),
            user.posts(), user.comments(), user.votes(),
            user.subscribedSubreddits(), user.moderatedSubreddits(),
            user.createdAt(), now
        );

        userRepository.save(updated);

        return new AdminAction(
            null, AdminActionRequest.AdminActionType.BAN_USER, userId, "User",
            reason, null, admin.id(), now, expiresAt, expiresAt == null, "User banned successfully"
        );
    }

    private AdminAction suspendUser(Long userId, String reason, Instant expiresAt, User admin, Instant now) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // In a real implementation, you'd have a suspension field
        // For now, we'll just create the action record

        return new AdminAction(
            null, AdminActionRequest.AdminActionType.SUSPEND_USER, userId, "User",
            reason, null, admin.id(), now, expiresAt, expiresAt == null, "User suspended"
        );
    }

    private AdminAction deletePost(Long postId, String reason, User admin, Instant now) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        // Soft delete by marking as removed
        Post updated = new Post(
            post.id(), post.title(), post.content(), post.url(), post.postType(),
            post.thumbnailUrl(), post.flairText(), post.flairCssClass(),
            post.isSpoiler(), post.isStickied(), post.isLocked(), post.isArchived(),
            post.isOver18(), true, // Mark as removed
            post.score(), post.upvoteCount(), post.downvoteCount(), post.commentCount(),
            post.viewCount(), post.awardCount(), post.author(), post.subreddit(),
            post.comments(), post.votes(), post.createdAt(), now, post.archivedAt()
        );

        postRepository.save(updated);

        return new AdminAction(
            null, AdminActionRequest.AdminActionType.DELETE_POST, postId, "Post",
            reason, null, admin.id(), now, null, true, "Post deleted"
        );
    }

    private AdminAction deleteComment(Long commentId, String reason, User admin, Instant now) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));

        // Soft delete by marking as removed
        Comment updated = new Comment(
            comment.id(), comment.content(), comment.flairText(), comment.isEdited(),
            comment.isDeleted(), true, // Mark as removed
            comment.isCollapsed(), comment.isStickied(), comment.isOver18(),
            comment.score(), comment.upvoteCount(), comment.downvoteCount(),
            comment.replyCount(), comment.awardCount(), comment.depthLevel(),
            comment.author(), comment.post(), comment.parent(), comment.replies(),
            comment.votes(), comment.createdAt(), now, comment.editedAt(),
            comment.deletedAt(), now
        );

        commentRepository.save(updated);

        return new AdminAction(
            null, AdminActionRequest.AdminActionType.DELETE_COMMENT, commentId, "Comment",
            reason, null, admin.id(), now, null, true, "Comment deleted"
        );
    }

    private AdminAction lockPost(Long postId, String reason, Instant expiresAt, User admin, Instant now) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        Post updated = new Post(
            post.id(), post.title(), post.content(), post.url(), post.postType(),
            post.thumbnailUrl(), post.flairText(), post.flairCssClass(),
            post.isSpoiler(), post.isStickied(), true, // Lock the post
            post.isArchived(), post.isOver18(), post.isRemoved(),
            post.score(), post.upvoteCount(), post.downvoteCount(), post.commentCount(),
            post.viewCount(), post.awardCount(), post.author(), post.subreddit(),
            post.comments(), post.votes(), post.createdAt(), now, post.archivedAt()
        );

        postRepository.save(updated);

        return new AdminAction(
            null, AdminActionRequest.AdminActionType.LOCK_POST, postId, "Post",
            reason, null, admin.id(), now, expiresAt, expiresAt == null, "Post locked"
        );
    }

    private AdminAction lockComment(Long commentId, String reason, Instant expiresAt, User admin, Instant now) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));

        Comment updated = new Comment(
            comment.id(), comment.content(), comment.flairText(), comment.isEdited(),
            comment.isDeleted(), comment.isRemoved(), comment.isCollapsed(),
            true, // Lock the comment
            comment.isOver18(), comment.score(), comment.upvoteCount(), comment.downvoteCount(),
            comment.replyCount(), comment.awardCount(), comment.depthLevel(),
            comment.author(), comment.post(), comment.parent(), comment.replies(),
            comment.votes(), comment.createdAt(), now, comment.editedAt(),
            comment.deletedAt(), comment.removedAt()
        );

        commentRepository.save(updated);

        return new AdminAction(
            null, AdminActionRequest.AdminActionType.LOCK_COMMENT, commentId, "Comment",
            reason, null, admin.id(), now, expiresAt, expiresAt == null, "Comment locked"
        );
    }

    private AdminAction removeModerator(Long userId, String reason, User admin, Instant now) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Remove from all moderated subreddits
        User updated = new User(
            user.id(), user.username(), user.email(), user.password(),
            user.displayName(), user.bio(), user.avatarUrl(),
            user.isActive(), user.isVerified(), user.karma(),
            user.posts(), user.comments(), user.votes(),
            user.subscribedSubreddits(), Set.of(), // Clear moderated subreddits
            user.createdAt(), now
        );

        userRepository.save(updated);

        return new AdminAction(
            null, AdminActionRequest.AdminActionType.REMOVE_MODERATOR, userId, "User",
            reason, null, admin.id(), now, null, true, "Moderator privileges removed"
        );
    }

    private AdminAction addModerator(Long userId, String reason, User admin, Instant now) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // In a real implementation, you'd need to specify which subreddit to add as moderator
        // For now, we'll just create the action record

        return new AdminAction(
            null, AdminActionRequest.AdminActionType.ADD_MODERATOR, userId, "User",
            reason, null, admin.id(), now, null, true, "Moderator privileges added"
        );
    }

    private AdminAction banSubreddit(Long subredditId, String reason, Instant expiresAt, User admin, Instant now) {
        Subreddit subreddit = subredditRepository.findById(subredditId)
            .orElseThrow(() -> new RuntimeException("Subreddit not found: " + subredditId));

        // In a real implementation, you'd have a banned field
        // For now, we'll just create the action record

        return new AdminAction(
            null, AdminActionRequest.AdminActionType.BAN_SUBREDDIT, subredditId, "Subreddit",
            reason, null, admin.id(), now, expiresAt, expiresAt == null, "Subreddit banned"
        );
    }

    private AdminAction quarantinePost(Long postId, String reason, User admin, Instant now) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found: " + postId));

        // Mark as quarantined (similar to removed but with different meaning)
        Post updated = new Post(
            post.id(), post.title(), post.content(), post.url(), post.postType(),
            post.thumbnailUrl(), post.flairText(), post.flairCssClass(),
            post.isSpoiler(), post.isStickied(), post.isLocked(), post.isArchived(),
            true, // Mark as over18/quarantined
            post.isRemoved(), post.score(), post.upvoteCount(), post.downvoteCount(),
            post.commentCount(), post.viewCount(), post.awardCount(), post.author(),
            post.subreddit(), post.comments(), post.votes(), post.createdAt(), now,
            post.archivedAt()
        );

        postRepository.save(updated);

        return new AdminAction(
            null, AdminActionRequest.AdminActionType.QUARANTINE_POST, postId, "Post",
            reason, null, admin.id(), now, null, true, "Post quarantined"
        );
    }

    private AdminAction quarantineComment(Long commentId, String reason, User admin, Instant now) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));

        // Mark as quarantined
        Comment updated = new Comment(
            comment.id(), comment.content(), comment.flairText(), comment.isEdited(),
            comment.isDeleted(), comment.isRemoved(), comment.isCollapsed(),
            comment.isStickied(), true, // Mark as over18/quarantined
            comment.score(), comment.upvoteCount(), comment.downvoteCount(),
            comment.replyCount(), comment.awardCount(), comment.depthLevel(),
            comment.author(), comment.post(), comment.parent(), comment.replies(),
            comment.votes(), comment.createdAt(), now, comment.editedAt(),
            comment.deletedAt(), comment.removedAt()
        );

        commentRepository.save(updated);

        return new AdminAction(
            null, AdminActionRequest.AdminActionType.QUARANTINE_COMMENT, commentId, "Comment",
            reason, null, admin.id(), now, null, true, "Comment quarantined"
        );
    }

    public Page<AdminActionResponse> getAdminActions(Pageable pageable) {
        Page<AdminAction> actions = adminActionRepository.findAllByOrderByCreatedAtDesc(pageable);
        return actions.map(AdminActionResponse::from);
    }

    public Page<AdminActionResponse> getAdminActionsByUser(Long userId, Pageable pageable) {
        Page<AdminAction> actions = adminActionRepository.findByPerformedByOrderByCreatedAtDesc(userId, pageable);
        return actions.map(AdminActionResponse::from);
    }

    public Page<AdminActionResponse> getActiveActions(Pageable pageable) {
        Page<AdminAction> actions = adminActionRepository.findByIsActiveOrderByCreatedAtDesc(true, pageable);
        return actions.map(AdminActionResponse::from);
    }

    private void sendActionNotification(AdminAction action) {
        try {
            String title = switch (action.type()) {
                case BAN_USER -> "Account Suspended";
                case SUSPEND_USER -> "Account Temporarily Suspended";
                case DELETE_POST -> "Post Removed";
                case DELETE_COMMENT -> "Comment Removed";
                case LOCK_POST -> "Post Locked";
                case LOCK_COMMENT -> "Comment Locked";
                case REMOVE_MODERATOR -> "Moderator Privileges Removed";
                case ADD_MODERATOR -> "Moderator Privileges Granted";
                case BAN_SUBREDDIT -> "Community Action Taken";
                case QUARANTINE_POST -> "Post Quarantined";
                case QUARANTINE_COMMENT -> "Comment Quarantined";
            };

            String message = String.format("Your content has been moderated. Reason: %s", action.reason());

            // Create notification for the affected user
            // This would need to be adapted based on the action type
        } catch (Exception e) {
            System.err.println("Failed to send admin action notification: " + e.getMessage());
        }
    }

    private void sendAuditEvent(AdminAction action) {
        try {
            // Send to audit log topic
            kafkaTemplate.send("admin-actions", action);
        } catch (Exception e) {
            System.err.println("Failed to send audit event: " + e.getMessage());
        }
    }
}
