package com.lambrk.dto;

import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.domain.Community;

import java.time.Instant;
import java.util.UUID;

public record PostResponse(
    
    UUID id,
    
    String title,
    
    String content,
    
    String url,
    
    Post.PostType postType,
    
    String thumbnailUrl,
    
    String flairText,
    
    String flairCssClass,
    
    boolean isSpoiler,
    
    boolean isStickied,
    
    boolean isLocked,
    
    boolean isArchived,
    
    boolean isOver18,
    
    int score,
    
    int likeCount,
    
    int dislikeCount,
    
    int commentCount,
    
    int viewCount,
    
    int awardCount,
    
    UserResponse author,
    
    CommunityResponse community,
    
    Instant createdAt,
    
    Instant updatedAt,
    
    Instant archivedAt,
    
    String userVote // LIKE, DISLIKE, or null
) {
    
    public static PostResponse from(Post post, String userVote) {
        return new PostResponse(
            post.id(),
            post.title(),
            post.content(),
            post.url(),
            post.postType(),
            post.thumbnailUrl(),
            post.flairText(),
            post.flairCssClass(),
            post.isSpoiler(),
            post.isStickied(),
            post.isLocked(),
            post.isArchived(),
            post.isOver18(),
            post.score(),
            post.likeCount(),
            post.dislikeCount(),
            post.commentCount(),
            post.viewCount(),
            post.awardCount(),
            UserResponse.from(post.author()),
            CommunityResponse.from(post.community()),
            post.createdAt(),
            post.updatedAt(),
            post.archivedAt(),
            userVote
        );
    }
    
    public static PostResponse from(Post post) {
        return from(post, null);
    }
}
