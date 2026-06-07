package com.lambrk.dto;

import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.domain.Community;

import java.time.Instant;
import java.util.List;
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

    String userVote, // LIKE, DISLIKE, or null

    List<MediaResponse> media
) {

    public static PostResponse from(Post post, String userVote) {
        List<MediaResponse> mediaList = post.getMedia() != null
            ? post.getMedia().stream().map(MediaResponse::from).toList()
            : List.of();

        return new PostResponse(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getUrl(),
            post.getPostType(),
            com.lambrk.util.CdnUrlResolver.resolve(post.getThumbnailUrl()),
            post.getFlairText(),
            post.getFlairCssClass(),
            post.isSpoiler(),
            post.isStickied(),
            post.isLocked(),
            post.isArchived(),
            post.isOver18(),
            post.getScore(),
            post.getLikeCount(),
            post.getDislikeCount(),
            post.getCommentCount(),
            post.getViewCount(),
            post.getAwardCount(),
            UserResponse.from(post.getAuthor()),
            post.getCommunity() != null ? CommunityResponse.from(post.getCommunity()) : null,
            post.getCreatedAt(),
            post.getUpdatedAt(),
            post.getArchivedAt(),
            userVote,
            mediaList
        );
    }
    
    public static PostResponse from(Post post) {
        return from(post, null);
    }
}
