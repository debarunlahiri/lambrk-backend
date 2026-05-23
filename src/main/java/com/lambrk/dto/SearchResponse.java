package com.lambrk.dto;

import java.util.List;

public record SearchResponse(
    
    List<PostResponse> posts,
    
    List<CommentResponse> comments,
    
    List<UserResponse> users,
    
    List<CommunityResponse> communities,
    
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
    
    public static SearchResponse ofCommunities(List<CommunityResponse> communities, SearchMetadata metadata) {
        return new SearchResponse(List.of(), List.of(), List.of(), communities, metadata);
    }
    
    public static SearchResponse ofAll(List<PostResponse> posts, List<CommentResponse> comments,
                                       List<UserResponse> users, List<CommunityResponse> communities,
                                       SearchMetadata metadata) {
        return new SearchResponse(posts, comments, users, communities, metadata);
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
