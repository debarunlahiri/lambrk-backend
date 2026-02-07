package com.lambrk.controller;

import com.lambrk.dto.RecommendationRequest;
import com.lambrk.dto.RecommendationResponse;
import com.lambrk.service.RecommendationService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    @NewSpan("get-recommendations")
    @Counted(value = "recommendations.generated")
    @Timed(value = "recommendations.duration")
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @Valid @RequestBody RecommendationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{userId}")
    @NewSpan("get-post-recommendations")
    @Counted(value = "recommendations.posts")
    @Timed(value = "recommendations.posts.duration")
    public ResponseEntity<RecommendationResponse> getPostRecommendations(
            @PathVariable @SpanTag Long userId,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) List<String> excludeSubreddits,
            @RequestParam(required = false) List<String> excludeUsers,
            @RequestParam(defaultValue = "false") boolean includeNSFW,
            @RequestParam(defaultValue = "false") boolean includeOver18,
            @RequestParam(required = false) String contextSubredditId,
            @RequestParam(required = false) String contextPostId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        RecommendationRequest request = new RecommendationRequest(
            userId,
            RecommendationRequest.RecommendationType.POSTS,
            limit,
            excludeSubreddits,
            excludeUsers,
            includeNSFW,
            includeOver18,
            contextSubredditId,
            contextPostId
        );
        
        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subreddits/{userId}")
    @NewSpan("get-subreddit-recommendations")
    @Counted(value = "recommendations.subreddits")
    @Timed(value = "recommendations.subreddits.duration")
    public ResponseEntity<RecommendationResponse> getSubredditRecommendations(
            @PathVariable @SpanTag Long userId,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) List<String> excludeSubreddits,
            @RequestParam(defaultValue = "false") boolean includeNSFW,
            @RequestParam(defaultValue = "false") boolean includeOver18,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        RecommendationRequest request = new RecommendationRequest(
            userId,
            RecommendationRequest.RecommendationType.SUBREDDITS,
            limit,
            excludeSubreddits,
            List.of(),
            includeNSFW,
            includeOver18,
            null,
            null
        );
        
        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    @NewSpan("get-user-recommendations")
    @Counted(value = "recommendations.users")
    @Timed(value = "recommendations.users.duration")
    public ResponseEntity<RecommendationResponse> getUserRecommendations(
            @PathVariable @SpanTag Long userId,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) List<String> excludeUsers,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        RecommendationRequest request = new RecommendationRequest(
            userId,
            RecommendationRequest.RecommendationType.USERS,
            limit,
            List.of(),
            excludeUsers,
            false,
            false,
            null,
            null
        );
        
        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/comments/{userId}")
    @NewSpan("get-comment-recommendations")
    @Counted(value = "recommendations.comments")
    @Timed(value = "recommendations.comments.duration")
    public ResponseEntity<RecommendationResponse> getCommentRecommendations(
            @PathVariable @SpanTag Long userId,
            @RequestParam(defaultValue = "20") Integer limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        RecommendationRequest request = new RecommendationRequest(
            userId,
            RecommendationRequest.RecommendationType.COMMENTS,
            limit,
            List.of(),
            List.of(),
            false,
            false,
            null,
            null
        );
        
        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/context/{userId}")
    @NewSpan("get-contextual-recommendations")
    @Counted(value = "recommendations.contextual")
    @Timed(value = "recommendations.contextual.duration")
    public ResponseEntity<RecommendationResponse> getContextualRecommendations(
            @PathVariable @SpanTag Long userId,
            @RequestParam(required = false) String contextSubredditId,
            @RequestParam(required = false) String contextPostId,
            @RequestParam(defaultValue = "posts") String type,
            @RequestParam(defaultValue = "20") Integer limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        RecommendationRequest.RecommendationType recType = switch (type.toLowerCase()) {
            case "posts" -> RecommendationRequest.RecommendationType.POSTS;
            case "subreddits" -> RecommendationRequest.RecommendationType.SUBREDDITS;
            case "users" -> RecommendationRequest.RecommendationType.USERS;
            case "comments" -> RecommendationRequest.RecommendationType.COMMENTS;
            default -> RecommendationRequest.RecommendationType.POSTS;
        };
        
        RecommendationRequest request = new RecommendationRequest(
            userId,
            recType,
            limit,
            List.of(),
            List.of(),
            false,
            false,
            contextSubredditId,
            contextPostId
        );
        
        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trending")
    @NewSpan("get-trending-recommendations")
    @Counted(value = "recommendations.trending")
    @Timed(value = "recommendations.trending.duration")
    public ResponseEntity<RecommendationResponse> getTrendingRecommendations(
            @RequestParam(defaultValue = "posts") String type,
            @RequestParam(defaultValue = "20") Integer limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // This would return trending content for all users
        RecommendationRequest.RecommendationType recType = switch (type.toLowerCase()) {
            case "posts" -> RecommendationRequest.RecommendationType.POSTS;
            case "subreddits" -> RecommendationRequest.RecommendationType.SUBREDDITS;
            case "users" -> RecommendationRequest.RecommendationType.USERS;
            case "comments" -> RecommendationRequest.RecommendationType.COMMENTS;
            default -> RecommendationRequest.RecommendationType.POSTS;
        };
        
        // Use a generic user ID for trending recommendations
        RecommendationRequest request = new RecommendationRequest(
            0L, // System user ID
            recType,
            limit,
            List.of(),
            List.of(),
            false,
            false,
            null,
            null
        );
        
        RecommendationResponse response = recommendationService.getRecommendations(request);
        return ResponseEntity.ok(response);
    }
}
