package com.lambrk.dto;

import com.lambrk.domain.Post.PostType;

import java.time.Instant;
import java.util.List;

public record FeedResponse(
    List<FeedPost> posts,
    List<SuggestedUser> suggestedUsers,
    FeedAlgorithmInfo algorithmInfo,
    Long totalAvailable,
    boolean hasMore
) {
    public record FeedPost(
        Long id,
        String title,
        String content,
        String url,
        PostType postType,
        String thumbnailUrl,
        String flairText,
        boolean isSpoiler,
        boolean isOver18,
        int score,
        int upvoteCount,
        int downvoteCount,
        int commentCount,
        int viewCount,
        double algorithmScore,
        List<String> reasons,
        PostUserInfo author,
        SubredditInfo subreddit,
        Instant createdAt,
        UserInteraction userInteraction
    ) {}

    public record PostUserInfo(
        Long id,
        String username,
        String displayName,
        String avatarUrl,
        int karma,
        boolean isVerified,
        UserType type
    ) {}

    public record SubredditInfo(
        Long id,
        String name,
        String title,
        String iconImageUrl,
        boolean isUserSubscribed
    ) {}

    public record SuggestedUser(
        Long id,
        String username,
        String displayName,
        String bio,
        String avatarUrl,
        int karma,
        boolean isVerified,
        UserType type,
        double relevanceScore,
        List<String> reasons,
        int mutualSubreddits,
        List<String> commonInterests
    ) {}

    public record UserInteraction(
        boolean hasUpvoted,
        boolean hasDownvoted,
        boolean hasCommented,
        boolean hasViewed,
        boolean isSaved,
        boolean isHidden,
        int viewCount,
        Instant lastInteractionAt
    ) {}

    public record FeedAlgorithmInfo(
        String sortMethod,
        double timeDecayFactor,
        int freshnessHours,
        List<String> factorsConsidered,
        long processingTimeMs
    ) {}

    public enum UserType {
        REGULAR,        // Normal user
        INFLUENCER,     // High karma/activity
        NEW_USER,       // Recently joined
        MODERATOR,      // Subreddit moderator
        ADMIN,          // Site admin
        VERIFIED        // Verified account
    }

    public static FeedResponse empty() {
        return new FeedResponse(
            List.of(),
            List.of(),
            new FeedAlgorithmInfo("none", 0.0, 0, List.of(), 0L),
            0L,
            false
        );
    }
}
