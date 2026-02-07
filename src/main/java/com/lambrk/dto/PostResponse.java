package com.lambrk.dto;

import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.domain.Subreddit;

import java.time.Instant;

public record PostResponse(
    
    Long id,
    
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
    
    int upvoteCount,
    
    int downvoteCount,
    
    int commentCount,
    
    int viewCount,
    
    int awardCount,
    
    UserResponse author,
    
    SubredditResponse subreddit,
    
    Instant createdAt,
    
    Instant updatedAt,
    
    Instant archivedAt,
    
    String userVote // UPVOTE, DOWNVOTE, or null
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
            post.upvoteCount(),
            post.downvoteCount(),
            post.commentCount(),
            post.viewCount(),
            post.awardCount(),
            UserResponse.from(post.author()),
            SubredditResponse.from(post.subreddit()),
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
