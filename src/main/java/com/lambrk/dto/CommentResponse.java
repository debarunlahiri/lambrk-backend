package com.lambrk.dto;

import com.lambrk.domain.Comment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommentResponse(
    UUID id,
    String content,
    String flairText,
    boolean isEdited,
    boolean isDeleted,
    boolean isRemoved,
    boolean isCollapsed,
    boolean isStickied,
    int score,
    int likeCount,
    int dislikeCount,
    int replyCount,
    int awardCount,
    int depthLevel,
    UserResponse author,
    UUID postId,
    UUID parentId,
    List<CommentResponse> replies,
    Instant createdAt,
    Instant updatedAt,
    Instant editedAt,
    String userVote
) {

    public static CommentResponse from(Comment comment, String userVote) {
        return new CommentResponse(
            comment.id(),
            comment.isDeleted() ? "[deleted]" : comment.content(),
            comment.flairText(),
            comment.isEdited(),
            comment.isDeleted(),
            comment.isRemoved(),
            comment.isCollapsed(),
            comment.isStickied(),
            comment.score(),
            comment.likeCount(),
            comment.dislikeCount(),
            comment.replyCount(),
            comment.awardCount(),
            comment.depthLevel(),
            UserResponse.from(comment.author()),
            comment.post().id(),
            comment.parent() != null ? comment.parent().id() : null,
            List.of(),
            comment.createdAt(),
            comment.updatedAt(),
            comment.editedAt(),
            userVote
        );
    }

    public static CommentResponse from(Comment comment) {
        return from(comment, null);
    }
}
