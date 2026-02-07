package com.lambrk.dto;

import java.util.List;

public record SearchResponse(
    
    List<PostResponse> posts,
    
    List<CommentResponse> comments,
    
    List<UserResponse> users,
    
    List<SubredditResponse> subreddits,
    
    SearchMetadata metadata
) {
    
    public static SearchResponse ofPosts(List<PostResponse> posts, SearchMetadata metadata) {
        return new SearchResponse(posts, List.of(), List.of(), List.of(), metadata);
    }
    
    public static SearchResponse ofComments(List<CommentResponse> comments, SearchMetadata metadata) {
        return new SearchResponse(List.of(), comments, List.of(), List.of(), metadata);
    }
    
    public static SearchResponse ofUsers(List<UserResponse> users, SearchMetadata metadata) {
        return new SearchResponse(List.of(), List.of(), users, List.of(), metadata);
    }
    
    public static SearchResponse ofSubreddits(List<SubredditResponse> subreddits, SearchMetadata metadata) {
        return new SearchResponse(List.of(), List.of(), List.of(), subreddits, metadata);
    }
    
    public static SearchResponse ofAll(List<PostResponse> posts, List<CommentResponse> comments,
                                       List<UserResponse> users, List<SubredditResponse> subreddits,
                                       SearchMetadata metadata) {
        return new SearchResponse(posts, comments, users, subreddits, metadata);
    }
    
    public record SearchMetadata(
        String query,
        SearchRequest.SearchType type,
        SearchRequest.SortBy sort,
        SearchRequest.TimeFilter timeFilter,
        int totalResults,
        int pageNumber,
        int pageSize,
        int totalPages,
        long searchTimeMs,
        List<String> suggestions
    ) {}
}
