package com.lambrk.dto;

import java.util.List;

public record RecommendationResponse(
    
    RecommendationRequest.RecommendationType type,
    
    List<PostResponse> posts,
    
    List<SubredditResponse> subreddits,
    
    List<UserResponse> users,
    
    List<CommentResponse> comments,
    
    String explanation,
    
    double confidence,
    
    List<String> factors
) {
    
    public static RecommendationResponse ofPosts(List<PostResponse> posts, String explanation, double confidence, List<String> factors) {
        return new RecommendationResponse(RecommendationRequest.RecommendationType.POSTS, posts, List.of(), List.of(), List.of(), explanation, confidence, factors);
    }
    
    public static RecommendationResponse ofSubreddits(List<SubredditResponse> subreddits, String explanation, double confidence, List<String> factors) {
        return new RecommendationResponse(RecommendationRequest.RecommendationType.SUBREDDITS, List.of(), subreddits, List.of(), List.of(), explanation, confidence, factors);
    }
    
    public static RecommendationResponse ofUsers(List<UserResponse> users, String explanation, double confidence, List<String> factors) {
        return new RecommendationResponse(RecommendationRequest.RecommendationType.USERS, List.of(), List.of(), users, List.of(), explanation, confidence, factors);
    }
    
    public static RecommendationResponse ofComments(List<CommentResponse> comments, String explanation, double confidence, List<String> factors) {
        return new RecommendationResponse(RecommendationRequest.RecommendationType.COMMENTS, List.of(), List.of(), List.of(), comments, explanation, confidence, factors);
    }
}
