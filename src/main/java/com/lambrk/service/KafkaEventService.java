package com.lambrk.service;

import com.lambrk.domain.Post;
import com.lambrk.domain.Comment;
import com.lambrk.domain.Vote;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
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
            post.subreddit().id(),
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
            post.subreddit().id(),
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
        Long postId,
        String title,
        Long authorId,
        Long subredditId,
        Instant timestamp,
        String eventType
    ) {}

    public record CommentEvent(
        Long commentId,
        String content,
        Long authorId,
        Long postId,
        Long parentCommentId,
        Instant timestamp,
        String eventType
    ) {}

    public record VoteEvent(
        Long voteId,
        String voteType,
        Long userId,
        Long postId,
        Long commentId,
        Instant timestamp,
        String eventType
    ) {}
}
