package com.lambrk.service;

import com.lambrk.domain.Comment;
import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.dto.CommentCreateRequest;
import com.lambrk.dto.CommentResponse;
import com.lambrk.dto.NotificationRequest;
import com.lambrk.exception.ResourceNotFoundException;
import com.lambrk.exception.UnauthorizedActionException;
import com.lambrk.repository.CommentRepository;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.UserRepository;
import com.lambrk.repository.VoteRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentService {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final VoteRepository voteRepository;
  private final KafkaEventService kafkaEventService;
  private final NotificationService notificationService;
  private final CustomMetrics customMetrics;

  public CommentService(
      CommentRepository commentRepository,
      PostRepository postRepository,
      UserRepository userRepository,
      VoteRepository voteRepository,
      KafkaEventService kafkaEventService,
      NotificationService notificationService,
      CustomMetrics customMetrics) {
    this.commentRepository = commentRepository;
    this.postRepository = postRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.kafkaEventService = kafkaEventService;
    this.notificationService = notificationService;
    this.customMetrics = customMetrics;
  }

  @RateLimiter(name = "commentCreation")
  @CircuitBreaker(name = "commentService")
  @Retry(name = "commentService")
  @CacheEvict(
      value = {"comments", "commentTrees"},
      allEntries = true)
  @ModerateContent(contentType = "comment")
  public CommentResponse createComment(CommentCreateRequest request, UUID authorId) {
    User author =
        userRepository
            .findById(authorId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

    Post post =
        postRepository
            .findById(request.postId())
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", request.postId()));

    if (post.isLocked()) {
      throw new UnauthorizedActionException("Cannot comment on a locked post");
    }

    Comment parent = null;
    if (request.parentCommentId() != null) {
      parent =
          commentRepository
              .findById(request.parentCommentId())
              .orElseThrow(
                  () -> new ResourceNotFoundException("Comment", "id", request.parentCommentId()));
    }

    Comment comment = new Comment(request.content(), author, post, parent);
    Comment saved = commentRepository.save(comment);

    postRepository.updatePostCommentCount(post.getId(), 1);
    if (parent != null) {
      commentRepository.updateCommentReplyCount(parent.getId(), 1);
    }

    String communityName = post.getCommunity() != null ? post.getCommunity().getName() : "direct";
    customMetrics.recordCommentCreated(communityName);
    kafkaEventService.sendCommentCreatedEvent(saved);

    // Send reply notification if this is a reply to another comment
    if (parent != null) {
      notificationService.createCommentReplyNotification(saved.getId(), post.getId(), authorId);
    }

    // Process @mentions
    processMentions(saved, author);

    return CommentResponse.from(saved);
  }

  @RateLimiter(name = "commentCreation")
  @CircuitBreaker(name = "commentService")
  @Retry(name = "commentService")
  @CacheEvict(
      value = {"comments", "commentTrees"},
      allEntries = true)
  @ModerateContent(contentType = "comment")
  public CommentResponse createReply(UUID parentCommentId, String content, UUID authorId) {
    User author =
        userRepository
            .findById(authorId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

    Comment parent =
        commentRepository
            .findById(parentCommentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", parentCommentId));

    Post post = parent.getPost();
    if (post.isLocked()) {
      throw new UnauthorizedActionException("Cannot reply on a locked post");
    }

    Comment comment = new Comment(content, author, post, parent);
    Comment saved = commentRepository.save(comment);

    postRepository.updatePostCommentCount(post.getId(), 1);
    commentRepository.updateCommentReplyCount(parent.getId(), 1);

    String communityName = post.getCommunity() != null ? post.getCommunity().getName() : "direct";
    customMetrics.recordCommentCreated(communityName);
    kafkaEventService.sendCommentCreatedEvent(saved);
    notificationService.createCommentReplyNotification(saved.getId(), post.getId(), authorId);

    // Process @mentions
    processMentions(saved, author);

    return CommentResponse.from(saved);
  }

  @Cacheable(value = "comments", key = "#commentId")
  @Transactional(readOnly = true)
  public CommentResponse getComment(UUID commentId, UUID currentUserId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
    String userVote = getUserVote(comment, currentUserId);
    return CommentResponse.from(comment, userVote);
  }

  @Cacheable(value = "commentTrees", key = "#postId + '-' + #pageable.pageNumber")
  @Transactional(readOnly = true)
  public Page<CommentResponse> getCommentsByPost(
      UUID postId, Pageable pageable, UUID currentUserId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
    return commentRepository
        .findByPostAndParentIsNull(post, pageable)
        .map(c -> toCommentResponseWithReplies(c, currentUserId, 3));
  }

  private CommentResponse toCommentResponseWithReplies(
      Comment comment, UUID currentUserId, int maxReplies) {
    String userVote = getUserVote(comment, currentUserId);
    List<CommentResponse> replyPreview = List.of();
    if (comment.getReplyCount() > 0 && maxReplies > 0) {
      replyPreview =
          commentRepository.findByParent(comment).stream()
              .limit(maxReplies)
              .map(c -> CommentResponse.from(c, getUserVote(c, currentUserId)))
              .toList();
    }
    return CommentResponse.from(comment, userVote, replyPreview);
  }

  @Transactional(readOnly = true)
  public List<CommentResponse> getReplies(UUID commentId, UUID currentUserId) {
    Comment parent =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
    return commentRepository.findByParent(parent).stream()
        .map(c -> CommentResponse.from(c, getUserVote(c, currentUserId)))
        .toList();
  }

  @Transactional(readOnly = true)
  public Page<CommentResponse> getCommentsByUser(
      UUID userId, Pageable pageable, UUID currentUserId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    return commentRepository
        .findByAuthor(user, pageable)
        .map(c -> CommentResponse.from(c, getUserVote(c, currentUserId)));
  }

  @CacheEvict(
      value = {"comments", "commentTrees"},
      allEntries = true)
  public CommentResponse updateComment(UUID commentId, String newContent, UUID currentUserId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

    if (!comment.getAuthor().getId().equals(currentUserId)) {
      throw new UnauthorizedActionException("You can only edit your own comments");
    }

    Comment updated =
        new Comment(
            comment.getId(),
            newContent,
            comment.getFlairText(),
            true,
            comment.isDeleted(),
            comment.isRemoved(),
            comment.isCollapsed(),
            comment.isStickied(),
            comment.isOver18(),
            comment.getScore(),
            comment.getLikeCount(),
            comment.getDislikeCount(),
            comment.getReplyCount(),
            comment.getAwardCount(),
            comment.getDepthLevel(),
            comment.getAuthor(),
            comment.getPost(),
            comment.getParent(),
            comment.getReplies(),
            comment.getVotes(),
            comment.getCreatedAt(),
            Instant.now(),
            Instant.now(),
            comment.getDeletedAt(),
            comment.getRemovedAt());
    Comment saved = commentRepository.save(updated);
    return CommentResponse.from(saved);
  }

  @CacheEvict(
      value = {"comments", "commentTrees"},
      allEntries = true)
  public void deleteComment(UUID commentId, UUID currentUserId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

    if (!comment.getAuthor().getId().equals(currentUserId)) {
      throw new UnauthorizedActionException("You can only delete your own comments");
    }

    commentRepository.softDeleteComment(commentId, Instant.now());
    postRepository.updatePostCommentCount(comment.getPost().getId(), -1);
  }

  @Transactional(readOnly = true)
  public Page<CommentResponse> searchComments(String query, Pageable pageable, UUID currentUserId) {
    return commentRepository
        .searchComments(query, pageable)
        .map(c -> CommentResponse.from(c, getUserVote(c, currentUserId)));
  }

  private void processMentions(Comment comment, User author) {
    java.util.regex.Pattern pattern =
        java.util.regex.Pattern.compile("(?<!\\w)@([a-zA-Z0-9_]+)(?!\\w)");
    java.util.regex.Matcher matcher = pattern.matcher(comment.getContent());
    java.util.Set<String> usernames = new java.util.HashSet<>();
    while (matcher.find()) {
      usernames.add(matcher.group(1).toLowerCase());
    }
    for (String username : usernames) {
      userRepository
          .findByUsername(username)
          .ifPresent(
              mentionedUser -> {
                if (!mentionedUser.getId().equals(author.getId())) {
                  String preview =
                      comment.getContent().length() > 100
                          ? comment.getContent().substring(0, 100) + "..."
                          : comment.getContent();
                  NotificationRequest notification =
                      new NotificationRequest(
                          NotificationRequest.NotificationType.COMMENT_MENTION,
                          mentionedUser.getId(),
                          "You were mentioned in a comment",
                          String.format("%s mentioned you: \"%s\"", author.getUsername(), preview),
                          comment.getPost().getId(),
                          comment.getId(),
                          author.getId(),
                          "/posts/" + comment.getPost().getId() + "#comment-" + comment.getId(),
                          "View mention",
                          false);
                  try {
                    notificationService.createNotification(notification);
                  } catch (Exception e) {
                    // Silently ignore notification failures
                  }
                }
              });
    }
  }

  private String getUserVote(Comment comment, UUID currentUserId) {
    if (currentUserId == null) return null;
    User user = userRepository.findById(currentUserId).orElse(null);
    if (user == null) return null;
    return voteRepository
        .findByUserAndComment(user, comment)
        .map(v -> v.getVoteType().name())
        .orElse(null);
  }
}
