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
        this.type = type != null ? type : RecommendationType.POSTS;
        this.limit = limit != null ? limit : 20;
        this.excludeSubreddits = excludeSubreddits != null ? excludeSubreddits : List.of();
        this.excludeUsers = excludeUsers != null ? excludeUsers : List.of();
        this.includeNSFW = false;
        this.includeOver18 = false;
    }
    
    public enum RecommendationType {
        POSTS, SUBREDDITS, USERS, COMMENTS
    }
}
