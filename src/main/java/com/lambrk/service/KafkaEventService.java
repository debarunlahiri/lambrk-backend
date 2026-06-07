package com.lambrk.service;

import com.lambrk.domain.Comment;
import com.lambrk.domain.Post;
import com.lambrk.domain.Vote;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Profile("!test")
public class KafkaEventService {

  private static final Logger logger = LoggerFactory.getLogger(KafkaEventService.class);

  private final StreamBridge streamBridge;

  public KafkaEventService(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Async
  public void sendPostCreatedEvent(Post post) {
    try {
      PostEvent event =
          new PostEvent(
              post.getId(),
              post.getTitle(),
              post.getAuthor().getId(),
              post.getCommunity() != null ? post.getCommunity().getId() : null,
              post.getCreatedAt(),
              "POST_CREATED");
      streamBridge.send("postCreated-out-0", event);
    } catch (Throwable t) {
      logger.warn("Failed to send postCreated event for post {}: {}", post.getId(), t.getMessage());
    }
  }

  @Async
  public void sendPostUpdatedEvent(Post post) {
    try {
      PostEvent event =
          new PostEvent(
              post.getId(),
              post.getTitle(),
              post.getAuthor().getId(),
              post.getCommunity() != null ? post.getCommunity().getId() : null,
              post.getUpdatedAt(),
              "POST_UPDATED");
      streamBridge.send("postUpdated-out-0", event);
    } catch (Throwable t) {
      logger.warn("Failed to send postUpdated event for post {}: {}", post.getId(), t.getMessage());
    }
  }

  @Async
  public void sendCommentCreatedEvent(Comment comment) {
    try {
      CommentEvent event =
          new CommentEvent(
              comment.getId(),
              comment.getContent(),
              comment.getAuthor().getId(),
              comment.getPost().getId(),
              comment.getParent() != null ? comment.getParent().getId() : null,
              comment.getCreatedAt(),
              "COMMENT_CREATED");
      streamBridge.send("commentCreated-out-0", event);
    } catch (Throwable t) {
      logger.warn(
          "Failed to send commentCreated event for comment {}: {}",
          comment.getId(),
          t.getMessage());
    }
  }

  @Async
  public void sendVoteCastEvent(Vote vote) {
    try {
      VoteEvent event =
          new VoteEvent(
              vote.getId(),
              vote.getVoteType().name(),
              vote.getUser().getId(),
              vote.getPost() != null ? vote.getPost().getId() : null,
              vote.getComment() != null ? vote.getComment().getId() : null,
              vote.getCreatedAt(),
              "VOTE_CAST");
      streamBridge.send("voteCast-out-0", event);
    } catch (Throwable t) {
      logger.warn("Failed to send voteCast event for vote {}: {}", vote.getId(), t.getMessage());
    }
  }

  public record PostEvent(
      UUID postId,
      String title,
      UUID authorId,
      UUID communityId,
      Instant timestamp,
      String eventType) {}

  public record CommentEvent(
      UUID commentId,
      String content,
      UUID authorId,
      UUID postId,
      UUID parentCommentId,
      Instant timestamp,
      String eventType) {}

  public record VoteEvent(
      UUID voteId,
      String voteType,
      UUID userId,
      UUID postId,
      UUID commentId,
      Instant timestamp,
      String eventType) {}
}
