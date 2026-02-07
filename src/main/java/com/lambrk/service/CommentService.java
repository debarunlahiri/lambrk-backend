package com.lambrk.service;

import com.lambrk.domain.Comment;
import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.dto.CommentCreateRequest;
import com.lambrk.dto.CommentResponse;
import com.lambrk.exception.ResourceNotFoundException;
import com.lambrk.exception.UnauthorizedActionException;
import com.lambrk.repository.CommentRepository;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.UserRepository;
import com.lambrk.repository.VoteRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final KafkaEventService kafkaEventService;
    private final CustomMetrics customMetrics;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository,
                         UserRepository userRepository, VoteRepository voteRepository,
                         KafkaEventService kafkaEventService, CustomMetrics customMetrics) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.kafkaEventService = kafkaEventService;
        this.customMetrics = customMetrics;
    }

    @RateLimiter(name = "commentCreation")
    @CircuitBreaker(name = "commentService")
    @Retry(name = "commentService")
    @CacheEvict(value = {"comments", "commentTrees"}, allEntries = true)
    @ModerateContent(contentType = "comment")
    public CommentResponse createComment(CommentCreateRequest request, Long authorId) {
        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        Post post = postRepository.findById(request.postId())
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", request.postId()));

        if (post.isLocked()) {
            throw new UnauthorizedActionException("Cannot comment on a locked post");
        }

        Comment parent = null;
        if (request.parentCommentId() != null) {
            parent = commentRepository.findById(request.parentCommentId())
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", request.parentCommentId()));
        }

        Comment comment = new Comment(request.content(), author, post, parent);
        Comment saved = commentRepository.save(comment);

        postRepository.updatePostCommentCount(post.id(), 1);
        if (parent != null) {
            commentRepository.updateCommentReplyCount(parent.id(), 1);
        }
        userRepository.updateUserKarma(authorId, 1);

        customMetrics.recordCommentCreated(post.subreddit().name());
        kafkaEventService.sendCommentCreatedEvent(saved);

        return CommentResponse.from(saved);
    }

    @Cacheable(value = "comments", key = "#commentId")
    @Transactional(readOnly = true)
    public CommentResponse getComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        String userVote = getUserVote(comment, currentUserId);
        return CommentResponse.from(comment, userVote);
    }

    @Cacheable(value = "commentTrees", key = "#postId + '-' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPost(Long postId, Pageable pageable, Long currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        return commentRepository.findByPostAndParentIsNull(post, pageable)
            .map(c -> CommentResponse.from(c, getUserVote(c, currentUserId)));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getReplies(Long commentId, Long currentUserId) {
        Comment parent = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        return commentRepository.findByParent(parent).stream()
            .map(c -> CommentResponse.from(c, getUserVote(c, currentUserId)))
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByUser(Long userId, Pageable pageable, Long currentUserId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return commentRepository.findByAuthor(user, pageable)
            .map(c -> CommentResponse.from(c, getUserVote(c, currentUserId)));
    }

    @CacheEvict(value = {"comments", "commentTrees"}, allEntries = true)
    public CommentResponse updateComment(Long commentId, String newContent, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.author().id().equals(currentUserId)) {
            throw new UnauthorizedActionException("You can only edit your own comments");
        }

        Comment updated = new Comment(
            comment.id(), newContent, comment.flairText(), true, comment.isDeleted(),
            comment.isRemoved(), comment.isCollapsed(), comment.isStickied(), comment.isOver18(),
            comment.score(), comment.upvoteCount(), comment.downvoteCount(), comment.replyCount(),
            comment.awardCount(), comment.depthLevel(), comment.author(), comment.post(), comment.parent(),
            comment.replies(), comment.votes(), comment.createdAt(), Instant.now(), Instant.now(),
            comment.deletedAt(), comment.removedAt()
        );
        Comment saved = commentRepository.save(updated);
        return CommentResponse.from(saved);
    }

    @CacheEvict(value = {"comments", "commentTrees"}, allEntries = true)
    public void deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.author().id().equals(currentUserId)) {
            throw new UnauthorizedActionException("You can only delete your own comments");
        }

        commentRepository.softDeleteComment(commentId, Instant.now());
        postRepository.updatePostCommentCount(comment.post().id(), -1);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> searchComments(String query, Pageable pageable, Long currentUserId) {
        return commentRepository.searchComments(query, pageable)
            .map(c -> CommentResponse.from(c, getUserVote(c, currentUserId)));
    }

    private String getUserVote(Comment comment, Long currentUserId) {
        if (currentUserId == null) return null;
        User user = userRepository.findById(currentUserId).orElse(null);
        if (user == null) return null;
        return voteRepository.findByUserAndComment(user, comment)
            .map(v -> v.voteType().name())
            .orElse(null);
    }
}
