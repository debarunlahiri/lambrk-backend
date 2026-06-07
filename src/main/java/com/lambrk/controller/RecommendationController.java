package com.lambrk.controller;

import com.lambrk.config.UserPrincipal;
import com.lambrk.dto.RecommendationRequest;
import com.lambrk.dto.RecommendationResponse;
import com.lambrk.service.RecommendationService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
      @AuthenticationPrincipal UserPrincipal userDetails) {

    RecommendationResponse response =
        recommendationService.getRecommendations(request, getUserId(userDetails));
    return ResponseEntity.ok(response);
  }

  @GetMapping("/posts/{userId}")
  @NewSpan("get-post-recommendations")
  @Counted(value = "recommendations.posts")
  @Timed(value = "recommendations.posts.duration")
  public ResponseEntity<RecommendationResponse> getPostRecommendations(
      @PathVariable @SpanTag UUID userId,
      @RequestParam(defaultValue = "20") Integer limit,
      @RequestParam(required = false) List<String> excludeCommunities,
      @RequestParam(required = false) List<String> excludeUsers,
      @RequestParam(defaultValue = "false") boolean includeNSFW,
      @RequestParam(defaultValue = "false") boolean includeOver18,
      @RequestParam(required = false) String contextCommunityId,
      @RequestParam(required = false) String contextPostId,
      @AuthenticationPrincipal UserPrincipal userDetails) {

    RecommendationRequest request =
        new RecommendationRequest(
            userId,
            RecommendationRequest.RecommendationType.POSTS,
            limit,
            excludeCommunities,
            excludeUsers,
            includeNSFW,
            includeOver18,
            contextCommunityId,
            contextPostId);

    RecommendationResponse response =
        recommendationService.getRecommendations(request, getUserId(userDetails));
    return ResponseEntity.ok(response);
  }

  @GetMapping("/communities/{userId}")
  @NewSpan("get-community-recommendations")
  @Counted(value = "recommendations.communities")
  @Timed(value = "recommendations.communities.duration")
  public ResponseEntity<RecommendationResponse> getCommunityRecommendations(
      @PathVariable @SpanTag UUID userId,
      @RequestParam(defaultValue = "20") Integer limit,
      @RequestParam(required = false) List<String> excludeCommunities,
      @RequestParam(defaultValue = "false") boolean includeNSFW,
      @RequestParam(defaultValue = "false") boolean includeOver18,
      @AuthenticationPrincipal UserPrincipal userDetails) {

    RecommendationRequest request =
        new RecommendationRequest(
            userId,
            RecommendationRequest.RecommendationType.COMMUNITIES,
            limit,
            excludeCommunities,
            List.of(),
            includeNSFW,
            includeOver18,
            null,
            null);

    RecommendationResponse response =
        recommendationService.getRecommendations(request, getUserId(userDetails));
    return ResponseEntity.ok(response);
  }

  @GetMapping("/users/{userId}")
  @NewSpan("get-user-recommendations")
  @Counted(value = "recommendations.users")
  @Timed(value = "recommendations.users.duration")
  public ResponseEntity<RecommendationResponse> getUserRecommendations(
      @PathVariable @SpanTag UUID userId,
      @RequestParam(defaultValue = "20") Integer limit,
      @RequestParam(required = false) List<String> excludeUsers,
      @AuthenticationPrincipal UserPrincipal userDetails) {

    RecommendationRequest request =
        new RecommendationRequest(
            userId,
            RecommendationRequest.RecommendationType.USERS,
            limit,
            List.of(),
            excludeUsers,
            false,
            false,
            null,
            null);

    RecommendationResponse response =
        recommendationService.getRecommendations(request, getUserId(userDetails));
    return ResponseEntity.ok(response);
  }

  @GetMapping("/comments/{userId}")
  @NewSpan("get-comment-recommendations")
  @Counted(value = "recommendations.comments")
  @Timed(value = "recommendations.comments.duration")
  public ResponseEntity<RecommendationResponse> getCommentRecommendations(
      @PathVariable @SpanTag UUID userId,
      @RequestParam(defaultValue = "20") Integer limit,
      @AuthenticationPrincipal UserPrincipal userDetails) {

    RecommendationRequest request =
        new RecommendationRequest(
            userId,
            RecommendationRequest.RecommendationType.COMMENTS,
            limit,
            List.of(),
            List.of(),
            false,
            false,
            null,
            null);

    RecommendationResponse response =
        recommendationService.getRecommendations(request, getUserId(userDetails));
    return ResponseEntity.ok(response);
  }

  @GetMapping("/context/{userId}")
  @NewSpan("get-contextual-recommendations")
  @Counted(value = "recommendations.contextual")
  @Timed(value = "recommendations.contextual.duration")
  public ResponseEntity<RecommendationResponse> getContextualRecommendations(
      @PathVariable @SpanTag UUID userId,
      @RequestParam(required = false) String contextCommunityId,
      @RequestParam(required = false) String contextPostId,
      @RequestParam(defaultValue = "posts") String type,
      @RequestParam(defaultValue = "20") Integer limit,
      @AuthenticationPrincipal UserPrincipal userDetails) {

    RecommendationRequest.RecommendationType recType =
        switch (type.toLowerCase()) {
          case "posts" -> RecommendationRequest.RecommendationType.POSTS;
          case "communities" -> RecommendationRequest.RecommendationType.COMMUNITIES;
          case "users" -> RecommendationRequest.RecommendationType.USERS;
          case "comments" -> RecommendationRequest.RecommendationType.COMMENTS;
          default -> RecommendationRequest.RecommendationType.POSTS;
        };

    RecommendationRequest request =
        new RecommendationRequest(
            userId,
            recType,
            limit,
            List.of(),
            List.of(),
            false,
            false,
            contextCommunityId,
            contextPostId);

    RecommendationResponse response =
        recommendationService.getRecommendations(request, getUserId(userDetails));
    return ResponseEntity.ok(response);
  }

  @GetMapping("/trending")
  @NewSpan("get-trending-recommendations")
  @Counted(value = "recommendations.trending")
  @Timed(value = "recommendations.trending.duration")
  public ResponseEntity<RecommendationResponse> getTrendingRecommendations(
      @RequestParam(defaultValue = "posts") String type,
      @RequestParam(defaultValue = "20") Integer limit,
      @AuthenticationPrincipal UserPrincipal userDetails) {

    // This would return trending content for all users
    RecommendationRequest.RecommendationType recType =
        switch (type.toLowerCase()) {
          case "posts" -> RecommendationRequest.RecommendationType.POSTS;
          case "communities" -> RecommendationRequest.RecommendationType.COMMUNITIES;
          case "users" -> RecommendationRequest.RecommendationType.USERS;
          case "comments" -> RecommendationRequest.RecommendationType.COMMENTS;
          default -> RecommendationRequest.RecommendationType.POSTS;
        };

    // Use a generic user ID for trending recommendations
    RecommendationRequest request =
        new RecommendationRequest(
            java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"), // System user ID
            recType,
            limit,
            List.of(),
            List.of(),
            false,
            false,
            null,
            null);

    RecommendationResponse response =
        recommendationService.getRecommendations(request, getUserId(userDetails));
    return ResponseEntity.ok(response);
  }

  private UUID getUserId(UserPrincipal userPrincipal) {
    return userPrincipal != null ? userPrincipal.getUserId() : null;
  }
}
