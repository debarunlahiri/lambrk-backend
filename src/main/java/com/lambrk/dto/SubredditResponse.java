package com.lambrk.dto;

import com.lambrk.domain.Subreddit;

import java.time.Instant;

public record SubredditResponse(
    
    Long id,
    
    String name,
    
    String title,
    
    String description,
    
    String sidebarText,
    
    String headerImageUrl,
    
    String iconImageUrl,
    
    boolean isPublic,
    
    boolean isRestricted,
    
    boolean isOver18,
    
    int memberCount,
    
    int subscriberCount,
    
    int activeUserCount,
    
    UserResponse createdBy,
    
    Instant createdAt,
    
    Instant updatedAt,
    
    boolean isUserSubscribed,
    
    boolean isUserModerator
) {
    
    public static SubredditResponse from(Subreddit subreddit) {
        return new SubredditResponse(
            subreddit.id(),
            subreddit.name(),
            subreddit.title(),
            subreddit.description(),
            subreddit.sidebarText(),
            subreddit.headerImageUrl(),
            subreddit.iconImageUrl(),
            subreddit.isPublic(),
            subreddit.isRestricted(),
            subreddit.isOver18(),
            subreddit.memberCount(),
            subreddit.subscriberCount(),
            subreddit.activeUserCount(),
            UserResponse.from(subreddit.createdBy()),
            subreddit.createdAt(),
            subreddit.updatedAt(),
            false,
            false
        );
    }
    
    public static SubredditResponse from(Subreddit subreddit, boolean isUserSubscribed, boolean isUserModerator) {
        return new SubredditResponse(
            subreddit.id(),
            subreddit.name(),
            subreddit.title(),
            subreddit.description(),
            subreddit.sidebarText(),
            subreddit.headerImageUrl(),
            subreddit.iconImageUrl(),
            subreddit.isPublic(),
            subreddit.isRestricted(),
            subreddit.isOver18(),
            subreddit.memberCount(),
            subreddit.subscriberCount(),
            subreddit.activeUserCount(),
            UserResponse.from(subreddit.createdBy()),
            subreddit.createdAt(),
            subreddit.updatedAt(),
            isUserSubscribed,
            isUserModerator
        );
    }
}
