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
            comment.getId(),
            comment.isDeleted() ? "[deleted]" : comment.getContent(),
            comment.getFlairText(),
            comment.isEdited(),
            comment.isDeleted(),
            comment.isRemoved(),
            comment.isCollapsed(),
            comment.isStickied(),
            comment.getScore(),
            comment.getLikeCount(),
            comment.getDislikeCount(),
            comment.getReplyCount(),
            comment.getAwardCount(),
            comment.getDepthLevel(),
            UserResponse.from(comment.getAuthor()),
            comment.getPost().getId(),
            comment.getParent() != null ? comment.getParent().getId() : null,
            List.of(),
            comment.getCreatedAt(),
            comment.getUpdatedAt(),
            comment.getEditedAt(),
            userVote
        );
    }

    public static CommentResponse from(Comment comment) {
        return from(comment, null);
    }
}
