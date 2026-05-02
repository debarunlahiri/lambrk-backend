package com.lambrk.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RecommendationRequest(
    
    @NotNull
    Long userId,
    
    RecommendationType type,
    
    Integer limit,
    
    List<String> excludeSubreddits,
    
    List<String> excludeUsers,
    
    boolean includeNSFW,
    
    boolean includeOver18,
    
    String contextSubredditId,
    
    String contextPostId
) {
    
    public RecommendationRequest {
        type = type != null ? type : RecommendationType.POSTS;
        limit = limit != null ? limit : 20;
        excludeSubreddits = excludeSubreddits != null ? excludeSubreddits : List.of();
        excludeUsers = excludeUsers != null ? excludeUsers : List.of();
    }
    
    public enum RecommendationType {
        POSTS, SUBREDDITS, USERS, COMMENTS
    }
}
