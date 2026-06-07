package com.lambrk.service;

import com.lambrk.domain.Post;
import com.lambrk.domain.Community;
import com.lambrk.domain.User;
import com.lambrk.domain.Vote;
import com.lambrk.dto.FeedRequest;
import com.lambrk.dto.FeedResponse;
import com.lambrk.dto.PostResponse;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.CommunityRepository;
import com.lambrk.repository.UserRepository;
import com.lambrk.repository.VoteRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FeedService {

    private static final Logger logger = LoggerFactory.getLogger(FeedService.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final VoteRepository voteRepository;
    private final CustomMetrics customMetrics;

    public FeedService(PostRepository postRepository,
                      UserRepository userRepository,
                      CommunityRepository communityRepository,
                      VoteRepository voteRepository,
                      CustomMetrics customMetrics) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.voteRepository = voteRepository;
        this.customMetrics = customMetrics;
    }

    @Cacheable(
        value = "feed", 
        key = "#request.userId() + '-' + #request.sortBy() + '-' + #request.limit() + '-' + #request.includeNsfw()",
        unless = "#result == null || #result.posts().isEmpty()"
    )
    @CircuitBreaker(name = "feedService", fallbackMethod = "getFallbackFeed")
    @Retry(name = "feedService")
    @Timed(value = "feed.generation.duration", description = "Time taken to generate feed")
    public FeedResponse getPersonalizedFeed(FeedRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate request
            validateRequest(request);
            
            User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.userId()));

            // Gather user interaction data
            UserInteractionData interactionData = gatherUserInteractionData(user);
            
            // Get candidate posts based on user's communities and interactions
            List<ScoredPost> scoredPosts = scoreAndRankPosts(user, interactionData, request);
            
            // Get suggested users based on interactions
            List<FeedResponse.SuggestedUser> suggestedUsers = findSuggestedUsers(user, interactionData, request);
            
            // Convert to response format
            List<FeedResponse.FeedPost> feedPosts = scoredPosts.stream()
                .limit(request.limit())
                .map(sp -> convertToFeedPost(sp.post(), sp.score(), sp.reasons(), interactionData))
                .toList();

            long processingTime = System.currentTimeMillis() - startTime;
            
            FeedResponse.FeedAlgorithmInfo algorithmInfo = new FeedResponse.FeedAlgorithmInfo(
                request.sortBy(),
                request.timeDecayFactor(),
                24,
                List.of(
                    "User engagement history",
                    "Post popularity (likes/dislikes)",
                    "Time decay (freshness)",
                    "Community affinity",
                    "Content type preferences",
                    "Author reputation"
                ),
                processingTime
            );

            customMetrics.recordFeedGeneration(feedPosts.size(), suggestedUsers.size());

            return new FeedResponse(
                feedPosts,
                suggestedUsers,
                algorithmInfo,
                (long) scoredPosts.size(),
                scoredPosts.size() > request.limit()
            );

        } catch (Exception e) {
            customMetrics.recordFeedError();
            throw new RuntimeException("Failed to generate personalized feed", e);
        }
    }

    private UserInteractionData gatherUserInteractionData(User user) {
        // Get user's voting history
        List<Vote> userVotes = voteRepository.findByUser(user);
        Set<UUID> likedPostIds = userVotes.stream()
            .filter(v -> v.getVoteType() == Vote.VoteType.LIKE)
            .map(v -> v.getPost().getId())
            .collect(Collectors.toSet());
        Set<UUID> dislikedPostIds = userVotes.stream()
            .filter(v -> v.getVoteType() == Vote.VoteType.DISLIKE)
            .map(v -> v.getPost().getId())
            .collect(Collectors.toSet());

        // Get user's subscribed communities
        Set<Community> subscribedCommunities = communityRepository.findSubscribedCommunitiesByUser(user.getId());
        Set<UUID> subscribedCommunityIds = subscribedCommunities.stream()
            .map(Community::getId)
            .collect(Collectors.toSet());

        // Get user's post history
        List<Post> userPosts = postRepository.findByAuthor(user, PageRequest.of(0, 100)).getContent();
        Set<Post.PostType> preferredPostTypes = userPosts.stream()
            .map(Post::getPostType)
            .collect(Collectors.toSet());

        // Get active communities from post history
        Map<UUID, Integer> communityActivityScore = new HashMap<>();
        userPosts.forEach(post -> {
            UUID communityId = post.getCommunity().getId();
            communityActivityScore.merge(communityId, 1, (a, b) -> a + b);
        });

        return new UserInteractionData(
            likedPostIds,
            dislikedPostIds,
            subscribedCommunityIds,
            subscribedCommunities,
            preferredPostTypes,
            communityActivityScore,
            userPosts
        );
    }

    private List<ScoredPost> scoreAndRankPosts(User user, UserInteractionData interactionData, FeedRequest request) {
        // Get candidate posts from subscribed communities and popular posts
        Pageable pageable = PageRequest.of(0, request.limit() * 3, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Post> candidatePosts;
        
        if (request.includeFromFollowingOnly() && !interactionData.subscribedCommunityIds().isEmpty()) {
            // Use findAll and filter since findByCommunityIdIn doesn't exist
            candidatePosts = postRepository.findAll(pageable).getContent()
                .stream()
                .filter(p -> interactionData.subscribedCommunityIds().contains(p.getCommunity().getId()))
                .collect(Collectors.toList());
        } else {
            candidatePosts = postRepository.findAll(pageable).getContent();
        }

        // Score each post
        List<ScoredPost> scoredPosts = candidatePosts.stream()
            .map(post -> {
                double score = calculatePostScore(post, interactionData, request);
                List<String> reasons = generateScoreReasons(post, interactionData, score);
                return new ScoredPost(post, score, reasons);
            })
            .filter(sp -> sp.score() > 0) // Filter out posts with 0 or negative scores
            .sorted(Comparator.comparingDouble(ScoredPost::score).reversed())
            .collect(Collectors.toList());

        return scoredPosts;
    }

    private double calculatePostScore(Post post, UserInteractionData interactionData, FeedRequest request) {
        double score = 0.0;
        
        // 1. Base popularity score (0-100)
        double popularityScore = calculatePopularityScore(post);
        score += popularityScore * 0.25;
        
        // 2. Time decay factor (freshness matters)
        double freshnessScore = calculateFreshnessScore(post, request.timeDecayFactor());
        score += freshnessScore * 0.20;
        
        // 3. Community affinity (0-100)
        double communityScore = calculateCommunityAffinity(post, interactionData);
        score += communityScore * 0.25;
        
        // 4. Content type preference (0-100)
        double contentTypeScore = calculateContentTypeScore(post, interactionData);
        score += contentTypeScore * 0.15;
        
        // 5. Author reputation (0-100)
        double authorScore = calculateAuthorScore(post.getAuthor());
        score += authorScore * 0.10;
        
        // 6. Personalization boosts
        if (interactionData.likedPostIds().contains(post.getId())) {
            score *= 0.3; // Penalize already seen/liked posts
        }
        if (interactionData.dislikedPostIds().contains(post.getId())) {
            score *= 0.1; // Heavily penalize disliked posts
        }
        
        return score;
    }

    private double calculatePopularityScore(Post post) {
        // Score based on likes, dislikes, comments, views
        int netVotes = post.getLikeCount() - post.getDislikeCount();
        int engagement = post.getCommentCount() + post.getViewCount() / 100;
        
        double score = Math.min(100, (netVotes * 2) + (engagement * 0.5));
        return Math.max(0, score);
    }

    private double calculateFreshnessScore(Post post, double timeDecayFactor) {
        Instant now = Instant.now();
        Duration age = Duration.between(post.getCreatedAt(), now);
        double hoursOld = age.toHours();
        
        // Exponential decay: score = 100 * e^(-λt)
        // where λ is the decay rate (higher = faster decay)
        double lambda = 0.05 * timeDecayFactor; // Default half-life ~14 hours
        double score = 100 * Math.exp(-lambda * hoursOld);
        
        return Math.max(0, Math.min(100, score));
    }

    private double calculateCommunityAffinity(Post post, UserInteractionData interactionData) {
        UUID communityId = post.getCommunity().getId();
        
        if (interactionData.subscribedCommunityIds().contains(communityId)) {
            return 100.0; // Subscribed community - high affinity
        }
        
        // Check if user has been active in this community
        Integer activityScore = interactionData.communityActivityScore().get(communityId);
        if (activityScore != null) {
            return Math.min(100, activityScore * 10.0); // 10 points per post
        }
        
        return 30.0; // Base score for new communities
    }

    private double calculateContentTypeScore(Post post, UserInteractionData interactionData) {
        if (interactionData.preferredPostTypes().isEmpty()) {
            return 50.0; // Neutral if no preference data
        }
        
        if (interactionData.preferredPostTypes().contains(post.getPostType())) {
            return 80.0 + (20.0 * Math.random()); // Boost for preferred types
        }
        
        return 40.0; // Slightly lower for non-preferred types
    }

    private double calculateAuthorScore(User author) {
        // Score based on author's karma and verification
        int karma = author.getKarma();
        double baseScore = Math.min(100, karma / 100.0); // 1 point per 100 karma
        
        if (author.isVerified()) {
            baseScore += 20;
        }
        
        return Math.min(100, baseScore);
    }

    private List<String> generateScoreReasons(Post post, UserInteractionData interactionData, double score) {
        List<String> reasons = new ArrayList<>();
        
        if (interactionData.subscribedCommunityIds().contains(post.getCommunity().getId())) {
            reasons.add("From your subscribed community");
        }
        
        if (post.getLikeCount() > 100) {
            reasons.add("Popular post");
        }
        
        if (post.getCommentCount() > 50) {
            reasons.add("Trending discussion");
        }
        
        Duration age = Duration.between(post.getCreatedAt(), Instant.now());
        if (age.toHours() < 6) {
            reasons.add("Fresh content");
        }
        
        if (interactionData.preferredPostTypes().contains(post.getPostType())) {
            reasons.add("Matches your content preferences");
        }
        
        if (post.getAuthor().isVerified()) {
            reasons.add("From verified user");
        }
        
        return reasons;
    }

    private List<FeedResponse.SuggestedUser> findSuggestedUsers(User user, UserInteractionData interactionData, FeedRequest request) {
        // Find users who post in similar communities
        Set<UUID> similarUsers = new HashSet<>();
        
        for (UUID communityId : interactionData.communityActivityScore().keySet()) {
            communityRepository.findById(communityId).ifPresent(sub -> {
                List<Post> postsInCommunity = postRepository.findByCommunity(sub, PageRequest.of(0, 20)).getContent();
                postsInCommunity.forEach(post -> {
                    if (!post.getAuthor().getId().equals(user.getId())) {
                        similarUsers.add(post.getAuthor().getId());
                    }
                });
            });
        }
        
        // Score and rank suggested users
        return similarUsers.stream()
            .map(userId -> userRepository.findById(userId).orElse(null))
            .filter(Objects::nonNull)
            .map(author -> calculateUserRelevance(author, user, interactionData))
            .sorted(Comparator.comparingDouble(FeedResponse.SuggestedUser::relevanceScore).reversed())
            .limit(5)
            .collect(Collectors.toList());
    }

    private FeedResponse.SuggestedUser calculateUserRelevance(User suggestedUser, User currentUser, UserInteractionData interactionData) {
        double score = 0.0;
        List<String> reasons = new ArrayList<>();
        int mutualCommunities = 0;
        List<String> commonInterests = new ArrayList<>();
        
        // Check mutual communities
        Set<Community> theirCommunities = communityRepository.findSubscribedCommunitiesByUser(suggestedUser.getId());
        for (Community sub : theirCommunities) {
            if (interactionData.subscribedCommunities().contains(sub)) {
                mutualCommunities++;
                commonInterests.add(sub.getName());
            }
        }
        
        if (mutualCommunities > 0) {
            score += mutualCommunities * 20.0;
            reasons.add("Active in " + mutualCommunities + " communities you follow");
        }
        
        // Author reputation
        if (suggestedUser.getKarma() > 1000) {
            score += 30.0;
            reasons.add("Active contributor");
        }
        
        if (suggestedUser.isVerified()) {
            score += 20.0;
            reasons.add("Verified user");
        }
        
        // Determine user type
        FeedResponse.UserType userType = determineUserType(suggestedUser);
        
        return new FeedResponse.SuggestedUser(
            suggestedUser.getId(),
            suggestedUser.getUsername(),
            suggestedUser.getDisplayName(),
            suggestedUser.getBio(),
            com.lambrk.util.CdnUrlResolver.resolve(suggestedUser.getAvatarUrl()),
            suggestedUser.getKarma(),
            suggestedUser.isVerified(),
            userType,
            Math.min(100, score),
            reasons,
            mutualCommunities,
            commonInterests.stream().limit(3).toList()
        );
    }

    private FeedResponse.UserType determineUserType(User user) {
        if (user.getKarma() > 10000) {
            return FeedResponse.UserType.INFLUENCER;
        } else if (user.isVerified()) {
            return FeedResponse.UserType.VERIFIED;
        } else {
            return FeedResponse.UserType.REGULAR;
        }
    }

    private FeedResponse.FeedPost convertToFeedPost(Post post, double score, List<String> reasons, UserInteractionData interactionData) {
        boolean isSubscribed = interactionData != null
            && interactionData.subscribedCommunityIds().contains(post.getCommunity().getId());
        boolean isLiked = interactionData != null
            && interactionData.likedPostIds().contains(post.getId());
        boolean isDisliked = interactionData != null
            && interactionData.dislikedPostIds().contains(post.getId());

        FeedResponse.PostUserInfo authorInfo = new FeedResponse.PostUserInfo(
            post.getAuthor().getId(),
            post.getAuthor().getUsername(),
            post.getAuthor().getDisplayName(),
            com.lambrk.util.CdnUrlResolver.resolve(post.getAuthor().getAvatarUrl()),
            post.getAuthor().getKarma(),
            post.getAuthor().isVerified(),
            determineUserType(post.getAuthor())
        );
        
        FeedResponse.CommunityInfo communityInfo = new FeedResponse.CommunityInfo(
            post.getCommunity().getId(),
            post.getCommunity().getName(),
            post.getCommunity().getTitle(),
            com.lambrk.util.CdnUrlResolver.resolve(post.getCommunity().getIconImageUrl()),
            isSubscribed
        );
        
        FeedResponse.UserInteraction userInteraction = new FeedResponse.UserInteraction(
            isLiked,
            isDisliked,
            false,
            false,
            false,
            false,
            0,
            null
        );
        
        return new FeedResponse.FeedPost(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getUrl(),
            post.getPostType(),
            com.lambrk.util.CdnUrlResolver.resolve(post.getThumbnailUrl()),
            post.getFlairText(),
            post.isSpoiler(),
            post.isOver18(),
            post.getScore(),
            post.getLikeCount(),
            post.getDislikeCount(),
            post.getCommentCount(),
            post.getViewCount(),
            score,
            reasons,
            authorInfo,
            communityInfo,
            post.getCreatedAt(),
            userInteraction
        );
    }

    // Record to hold user interaction data for scoring
    private record UserInteractionData(
        Set<UUID> likedPostIds,
        Set<UUID> dislikedPostIds,
        Set<UUID> subscribedCommunityIds,
        Set<Community> subscribedCommunities,
        Set<Post.PostType> preferredPostTypes,
        Map<UUID, Integer> communityActivityScore,
        List<Post> userPosts
    ) {}

    // Record to hold scored posts
    private record ScoredPost(Post post, double score, List<String> reasons) {}

    // Custom exceptions
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class FeedGenerationException extends RuntimeException {
        public FeedGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Validates the feed request parameters
     */
    private void validateRequest(FeedRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("FeedRequest cannot be null");
        }
        if (request.userId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request.limit() == null || request.limit() < 1 || request.limit() > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }
        if (request.timeDecayFactor() != null && (request.timeDecayFactor() < 0.1 || request.timeDecayFactor() > 5.0)) {
            throw new IllegalArgumentException("Time decay factor must be between 0.1 and 5.0");
        }
    }

    /**
     * Fallback method when feed generation fails
     */
    public FeedResponse getFallbackFeed(FeedRequest request, Exception ex) {
        logger.warn("Using fallback feed for user {} due to: {}", request.userId(), ex.getMessage());
        
        try {
            // Return simple popular posts as fallback
            Pageable pageable = PageRequest.of(0, request.limit(), Sort.by(Sort.Direction.DESC, "score"));
            List<Post> popularPosts = postRepository.findAll(pageable).getContent();
            
            List<FeedResponse.FeedPost> feedPosts = popularPosts.stream()
                .map(post -> convertToFeedPost(post, 50.0, List.of("Popular post"), null))
                .toList();
            
            FeedResponse.FeedAlgorithmInfo algorithmInfo = new FeedResponse.FeedAlgorithmInfo(
                "fallback",
                0.0,
                0,
                List.of("Fallback: Popular posts only"),
                0L
            );
            
            return new FeedResponse(
                feedPosts,
                List.of(),
                algorithmInfo,
                (long) feedPosts.size(),
                false
            );
        } catch (Exception fallbackEx) {
            logger.error("Fallback feed also failed: {}", fallbackEx.getMessage());
            return FeedResponse.empty();
        }
    }
}
