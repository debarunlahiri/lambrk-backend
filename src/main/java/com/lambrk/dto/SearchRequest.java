package com.lambrk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SearchRequest(
    
    @NotBlank(message = "Query is required")
    @Size(min = 2, max = 100, message = "Query must be between 2 and 100 characters")
    String query,
    
    SearchType type,
    
    SortBy sort,
    
    TimeFilter timeFilter,
    
    List<String> subreddits,
    
    List<String> flairs,
    
    boolean includeNSFW,
    
    boolean includeOver18,
    
    Integer minScore,
    
    Integer minComments,
    
    Integer minVotes,
    
    Integer page,
    
    Integer size
) {
    
    public SearchRequest {
        this.type = type != null ? type : SearchType.ALL;
        this.sort = sort != null ? sort : SortBy.RELEVANCE;
        this.timeFilter = timeFilter != null ? timeFilter : TimeFilter.ALL;
        this.subreddits = subreddits != null ? subreddits : List.of();
        this.flairs = flairs != null ? flairs : List.of();
        this.includeNSFW = false;
        this.includeOver18 = false;
        this.minScore = null;
        this.minComments = null;
        this.minVotes = null;
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 20;
    }
    
    public enum SearchType {
        ALL, POSTS, COMMENTS, USERS, SUBREDDITS
    }
    
    public enum SortBy {
        RELEVANCE, NEW, HOT, TOP, CONTROVERSIAL
    }
    
    public enum TimeFilter {
        ALL, HOUR, DAY, WEEK, MONTH, YEAR
    }
}
