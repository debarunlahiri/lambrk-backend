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
        PostEvent event = new PostEvent(
            post.id(),
            post.title(),
            post.author().id(),
            post.community().id(),
            post.createdAt(),
            "POST_CREATED"
        );
        streamBridge.send("postCreated-out-0", event);
    }

    public void sendPostUpdatedEvent(Post post) {
        PostEvent event = new PostEvent(
            post.id(),
            post.title(),
            post.author().id(),
            post.community().id(),
            post.updatedAt(),
            "POST_UPDATED"
        );
        streamBridge.send("postUpdated-out-0", event);
    }

    public void sendCommentCreatedEvent(Comment comment) {
        CommentEvent event = new CommentEvent(
            comment.id(),
            comment.content(),
            comment.author().id(),
            comment.post().id(),
            comment.parent() != null ? comment.parent().id() : null,
            comment.createdAt(),
            "COMMENT_CREATED"
        );
        streamBridge.send("commentCreated-out-0", event);
    }

    public void sendVoteCastEvent(Vote vote) {
        VoteEvent event = new VoteEvent(
            vote.id(),
            vote.voteType().name(),
            vote.user().id(),
            vote.post() != null ? vote.post().id() : null,
            vote.comment() != null ? vote.comment().id() : null,
            vote.createdAt(),
            "VOTE_CAST"
        );
        streamBridge.send("voteCast-out-0", event);
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
