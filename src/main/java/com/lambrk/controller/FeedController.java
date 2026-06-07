package com.lambrk.controller;

import com.lambrk.dto.FeedRequest;
import com.lambrk.dto.FeedResponse;
import com.lambrk.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.lambrk.config.UserPrincipal;
import java.util.UUID;

@RestController
@RequestMapping("/api/feed")
@Tag(name = "Feed", description = "Personalized feed API with algorithm-based content ranking")
@SecurityRequirement(name = "bearerAuth")
public class FeedController {

    private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    private UUID getUserIdFromUserDetails(UserPrincipal userPrincipal) {
        return userPrincipal != null ? userPrincipal.getUserId() : null;
    }

    @GetMapping
    @Operation(
        summary = "Get personalized feed",
        description = "Returns a personalized feed of posts and suggested users. Authenticated users get personalized ranking; anonymous users get trending content."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feed generated successfully"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<FeedResponse> getFeed(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @Parameter(description = "Number of posts to return (1-100, default 20)")
            @RequestParam(defaultValue = "20") Integer limit,
            @Parameter(description = "Sort method: algorithm, hot, new, top (default: algorithm)")
            @RequestParam(defaultValue = "algorithm") String sortBy,
            @Parameter(description = "Include NSFW content (default: false)")
            @RequestParam(defaultValue = "false") Boolean includeNsfw,
            @Parameter(description = "Only show posts from subscribed communities (default: false)")
            @RequestParam(defaultValue = "false") Boolean fromFollowingOnly,
            @Parameter(description = "Time decay factor for freshness (default: 1.0)")
            @RequestParam(defaultValue = "1.0") Double timeDecayFactor
    ) {
        UUID userId = getUserIdFromUserDetails(userDetails);
        logger.info("Generating personalized feed for user: {} with limit: {}", userId, limit);

        FeedRequest request = new FeedRequest(
            userId,
            limit,
            sortBy,
            null, // postTypes - accept all
            includeNsfw,
            fromFollowingOnly,
            timeDecayFactor
        );

        FeedResponse response = feedService.getPersonalizedFeed(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(
        summary = "Get personalized feed with filters",
        description = "Returns a personalized feed with advanced filtering options for post types and content preferences."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feed generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<FeedResponse> getFeedWithFilters(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @Valid @RequestBody FeedRequest request
    ) {
        UUID userId = getUserIdFromUserDetails(userDetails);
        logger.info("Generating personalized feed with filters for user: {}", userId);

        // Override the userId from the authenticated user
        FeedRequest authenticatedRequest = new FeedRequest(
            userId,
            request.limit(),
            request.sortBy(),
            request.postTypes(),
            request.includeNsfw(),
            request.includeFromFollowingOnly(),
            request.timeDecayFactor()
        );

        FeedResponse response = feedService.getPersonalizedFeed(authenticatedRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hot")
    @Operation(
        summary = "Get hot/trending feed",
        description = "Returns trending posts based on popularity and recent activity."
    )
    public ResponseEntity<FeedResponse> getHotFeed(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        UUID userId = getUserIdFromUserDetails(userDetails);
        logger.info("Generating hot feed for user: {} with limit: {}", userId, limit);

        FeedRequest request = new FeedRequest(
            userId,
            limit,
            "hot",
            null,
            false,
            false,
            2.0 // Faster time decay for hot content
        );

        FeedResponse response = feedService.getPersonalizedFeed(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/new")
    @Operation(
        summary = "Get newest posts feed",
        description = "Returns the most recent posts with minimal algorithmic ranking."
    )
    public ResponseEntity<FeedResponse> getNewFeed(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        UUID userId = getUserIdFromUserDetails(userDetails);
        logger.info("Generating new feed for user: {} with limit: {}", userId, limit);

        FeedRequest request = new FeedRequest(
            userId,
            limit,
            "new",
            null,
            false,
            false,
            0.1 // Minimal time decay for newest first
        );

        FeedResponse response = feedService.getPersonalizedFeed(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top")
    @Operation(
        summary = "Get top posts feed",
        description = "Returns highest scoring posts of all time or specified time period."
    )
    public ResponseEntity<FeedResponse> getTopFeed(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "all") String timePeriod
    ) {
        UUID userId = getUserIdFromUserDetails(userDetails);
        logger.info("Generating top feed for user: {} with limit: {} and timePeriod: {}",
            userId, limit, timePeriod);

        FeedRequest request = new FeedRequest(
            userId,
            limit,
            "top",
            null,
            false,
            false,
            0.5
        );

        FeedResponse response = feedService.getPersonalizedFeed(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/discover")
    @Operation(
        summary = "Discover new content",
        description = "Returns posts from communities the user doesn't follow to discover new content."
    )
    public ResponseEntity<FeedResponse> getDiscoverFeed(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        UUID userId = getUserIdFromUserDetails(userDetails);
        logger.info("Generating discover feed for user: {} with limit: {}", userId, limit);

        FeedRequest request = new FeedRequest(
            userId,
            limit,
            "discover",
            null,
            false,
            false,
            1.5
        );

        FeedResponse response = feedService.getPersonalizedFeed(request);
        return ResponseEntity.ok(response);
    }
}
