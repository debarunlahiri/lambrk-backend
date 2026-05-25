package com.lambrk.service;

import com.lambrk.domain.Post;
import com.lambrk.domain.Comment;
import com.lambrk.domain.Vote;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@Profile("!test")
public class KafkaEventService {

    private final StreamBridge streamBridge;

    public KafkaEventService(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendPostCreatedEvent(Post post) {
        try {
            PostEvent event = new PostEvent(
                post.getId(),
                post.getTitle(),
                post.getAuthor().getId(),
                post.getCommunity() != null ? post.getCommunity().getId() : null,
                post.getCreatedAt(),
                "POST_CREATED"
            );
            streamBridge.send("postCreated-out-0", event);
        } catch (Exception ignored) {
        }
    }

    public void sendPostUpdatedEvent(Post post) {
        try {
            PostEvent event = new PostEvent(
                post.getId(),
                post.getTitle(),
                post.getAuthor().getId(),
                post.getCommunity() != null ? post.getCommunity().getId() : null,
                post.getUpdatedAt(),
                "POST_UPDATED"
            );
            streamBridge.send("postUpdated-out-0", event);
        } catch (Exception ignored) {
        }
    }

    public void sendCommentCreatedEvent(Comment comment) {
        try {
            CommentEvent event = new CommentEvent(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getId(),
                comment.getPost().getId(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.getCreatedAt(),
                "COMMENT_CREATED"
            );
            streamBridge.send("commentCreated-out-0", event);
        } catch (Exception ignored) {
        }
    }

    public void sendVoteCastEvent(Vote vote) {
        try {
            VoteEvent event = new VoteEvent(
                vote.getId(),
                vote.getVoteType().name(),
                vote.getUser().getId(),
                vote.getPost() != null ? vote.getPost().getId() : null,
                vote.getComment() != null ? vote.getComment().getId() : null,
                vote.getCreatedAt(),
                "VOTE_CAST"
            );
            streamBridge.send("voteCast-out-0", event);
        } catch (Exception ignored) {
        }
    }

    public record PostEvent(
        UUID postId,
        String title,
        UUID authorId,
        UUID communityId,
        Instant timestamp,
        String eventType
    ) {}

    public record CommentEvent(
        UUID commentId,
        String content,
        UUID authorId,
        UUID postId,
        UUID parentCommentId,
        Instant timestamp,
        String eventType
    ) {}

    public record VoteEvent(
        UUID voteId,
        String voteType,
        UUID userId,
        UUID postId,
        UUID commentId,
        Instant timestamp,
        String eventType
    ) {}
}
