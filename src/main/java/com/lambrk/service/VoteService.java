package com.lambrk.service;

import com.lambrk.domain.Comment;
import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.domain.Vote;
import com.lambrk.dto.VoteRequest;
import com.lambrk.exception.ResourceNotFoundException;
import com.lambrk.repository.CommentRepository;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.UserRepository;
import com.lambrk.repository.VoteRepository;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class VoteService {

    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final KafkaEventService kafkaEventService;
    private final CustomMetrics customMetrics;

    public VoteService(VoteRepository voteRepository, PostRepository postRepository,
                      CommentRepository commentRepository, UserRepository userRepository,
                      KafkaEventService kafkaEventService, CustomMetrics customMetrics) {
        this.voteRepository = voteRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.kafkaEventService = kafkaEventService;
        this.customMetrics = customMetrics;
    }

    @RateLimiter(name = "voteCasting")
    @CacheEvict(value = {"posts", "comments", "hotPosts", "topPosts"}, allEntries = true)
    public void voteOnPost(VoteRequest request, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Post post = postRepository.findById(request.postId())
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", request.postId()));

        Optional<Vote> existingVote = voteRepository.findByUserAndPost(user, post);

        if (existingVote.isPresent()) {
            Vote old = existingVote.get();
            if (old.voteType() == request.voteType()) {
                // Remove vote (toggle off)
                voteRepository.delete(old);
                int scoreDelta = request.voteType() == Vote.VoteType.UPVOTE ? -1 : 1;
                int upDelta = request.voteType() == Vote.VoteType.UPVOTE ? -1 : 0;
                int downDelta = request.voteType() == Vote.VoteType.DOWNVOTE ? -1 : 0;
                postRepository.updatePostScore(post.id(), scoreDelta, upDelta, downDelta);
                userRepository.updateUserKarma(post.author().id(), scoreDelta);
            } else {
                // Flip vote
                Vote flipped = new Vote(old.id(), request.voteType(), user, post, null,
                    old.ipAddress(), old.userAgent(), old.createdAt(), java.time.Instant.now());
                voteRepository.save(flipped);
                int scoreDelta = request.voteType() == Vote.VoteType.UPVOTE ? 2 : -2;
                int upDelta = request.voteType() == Vote.VoteType.UPVOTE ? 1 : -1;
                int downDelta = request.voteType() == Vote.VoteType.DOWNVOTE ? 1 : -1;
                postRepository.updatePostScore(post.id(), scoreDelta, upDelta, downDelta);
                userRepository.updateUserKarma(post.author().id(), scoreDelta);
            }
        } else {
            Vote vote = new Vote(request.voteType(), user, post, null);
            Vote saved = voteRepository.save(vote);
            int scoreDelta = request.voteType() == Vote.VoteType.UPVOTE ? 1 : -1;
            int upDelta = request.voteType() == Vote.VoteType.UPVOTE ? 1 : 0;
            int downDelta = request.voteType() == Vote.VoteType.DOWNVOTE ? 1 : 0;
            postRepository.updatePostScore(post.id(), scoreDelta, upDelta, downDelta);
            userRepository.updateUserKarma(post.author().id(), scoreDelta);
            kafkaEventService.sendVoteCastEvent(saved);
        }

        customMetrics.recordVoteCast(request.voteType().name());
    }

    @RateLimiter(name = "voteCasting")
    @CacheEvict(value = {"comments", "commentTrees"}, allEntries = true)
    public void voteOnComment(VoteRequest request, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Comment comment = commentRepository.findById(request.commentId())
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", request.commentId()));

        Optional<Vote> existingVote = voteRepository.findByUserAndComment(user, comment);

        if (existingVote.isPresent()) {
            Vote old = existingVote.get();
            if (old.voteType() == request.voteType()) {
                voteRepository.delete(old);
                int scoreDelta = request.voteType() == Vote.VoteType.UPVOTE ? -1 : 1;
                int upDelta = request.voteType() == Vote.VoteType.UPVOTE ? -1 : 0;
                int downDelta = request.voteType() == Vote.VoteType.DOWNVOTE ? -1 : 0;
                commentRepository.updateCommentScore(comment.id(), scoreDelta, upDelta, downDelta);
                userRepository.updateUserKarma(comment.author().id(), scoreDelta);
            } else {
                Vote flipped = new Vote(old.id(), request.voteType(), user, null, comment,
                    old.ipAddress(), old.userAgent(), old.createdAt(), java.time.Instant.now());
                voteRepository.save(flipped);
                int scoreDelta = request.voteType() == Vote.VoteType.UPVOTE ? 2 : -2;
                int upDelta = request.voteType() == Vote.VoteType.UPVOTE ? 1 : -1;
                int downDelta = request.voteType() == Vote.VoteType.DOWNVOTE ? 1 : -1;
                commentRepository.updateCommentScore(comment.id(), scoreDelta, upDelta, downDelta);
                userRepository.updateUserKarma(comment.author().id(), scoreDelta);
            }
        } else {
            Vote vote = new Vote(request.voteType(), user, null, comment);
            Vote saved = voteRepository.save(vote);
            int scoreDelta = request.voteType() == Vote.VoteType.UPVOTE ? 1 : -1;
            int upDelta = request.voteType() == Vote.VoteType.UPVOTE ? 1 : 0;
            int downDelta = request.voteType() == Vote.VoteType.DOWNVOTE ? 1 : 0;
            commentRepository.updateCommentScore(comment.id(), scoreDelta, upDelta, downDelta);
            userRepository.updateUserKarma(comment.author().id(), scoreDelta);
            kafkaEventService.sendVoteCastEvent(saved);
        }

        customMetrics.recordVoteCast(request.voteType().name());
    }
}
