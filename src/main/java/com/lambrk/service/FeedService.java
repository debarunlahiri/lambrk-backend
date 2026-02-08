package com.lambrk.service;

import com.lambrk.domain.Post;
import com.lambrk.domain.Subreddit;
import com.lambrk.domain.User;
import com.lambrk.domain.Vote;
import com.lambrk.dto.FeedRequest;
import com.lambrk.dto.FeedResponse;
import com.lambrk.dto.PostResponse;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.SubredditRepository;
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
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FeedService {

    private static final Logger logger = LoggerFactory.getLogger(FeedService.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SubredditRepository subredditRepository;
    private final VoteRepository voteRepository;
    private final CustomMetrics customMetrics;

    public FeedService(PostRepository postRepository,
                      UserRepository userRepository,
                      SubredditRepository subredditRepository,
                      VoteRepository voteRepository,
                      CustomMetrics customMetrics) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.subredditRepository = subredditRepository;
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
            
            // Get candidate posts based on user's subreddits and interactions
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
                    "Post popularity (upvotes/downvotes)",
                    "Time decay (freshness)",
                    "Subreddit affinity",
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
        Set<Long> upvotedPostIds = userVotes.stream()
            .filter(v -> v.voteType() == Vote.VoteType.UPVOTE)
            .map(v -> v.post().id())
            .collect(Collectors.toSet());
        Set<Long> downvotedPostIds = userVotes.stream()
            .filter(v -> v.voteType() == Vote.VoteType.DOWNVOTE)
            .map(v -> v.post().id())
            .collect(Collectors.toSet());

        // Get user's subscribed subreddits
        Set<Subreddit> subscribedSubreddits = subredditRepository.findSubscribedSubredditsByUser(user.id());
        Set<Long> subscribedSubredditIds = subscribedSubreddits.stream()
            .map(Subreddit::id)
            .collect(Collectors.toSet());

        // Get user's post history
        List<Post> userPosts = postRepository.findByAuthor(user, PageRequest.of(0, 100)).getContent();
        Set<Post.PostType> preferredPostTypes = userPosts.stream()
            .map(Post::postType)
            .collect(Collectors.toSet());

        // Get active subreddits from post history
        Map<Long, Integer> subredditActivityScore = new HashMap<>();
        userPosts.forEach(post -> {
            Long subredditId = post.subreddit().id();
            subredditActivityScore.merge(subredditId, 1, (a, b) -> a + b);
        });

        return new UserInteractionData(
            upvotedPostIds,
            downvotedPostIds,
            subscribedSubredditIds,
            subscribedSubreddits,
            preferredPostTypes,
            subredditActivityScore,
            userPosts
        );
    }

    private List<ScoredPost> scoreAndRankPosts(User user, UserInteractionData interactionData, FeedRequest request) {
        // Get candidate posts from subscribed subreddits and popular posts
        Pageable pageable = PageRequest.of(0, request.limit() * 3, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Post> candidatePosts;
        
        if (request.includeFromFollowingOnly() && !interactionData.subscribedSubredditIds().isEmpty()) {
            // Use findAll and filter since findBySubredditIdIn doesn't exist
            candidatePosts = postRepository.findAll(pageable).getContent()
                .stream()
                .filter(p -> interactionData.subscribedSubredditIds().contains(p.subreddit().id()))
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
        
        // 3. Subreddit affinity (0-100)
        double subredditScore = calculateSubredditAffinity(post, interactionData);
        score += subredditScore * 0.25;
        
        // 4. Content type preference (0-100)
        double contentTypeScore = calculateContentTypeScore(post, interactionData);
        score += contentTypeScore * 0.15;
        
        // 5. Author reputation (0-100)
        double authorScore = calculateAuthorScore(post.author());
        score += authorScore * 0.10;
        
        // 6. Personalization boosts
        if (interactionData.upvotedPostIds().contains(post.id())) {
            score *= 0.3; // Penalize already seen/upvoted posts
        }
        if (interactionData.downvotedPostIds().contains(post.id())) {
            score *= 0.1; // Heavily penalize downvoted posts
        }
        
        return score;
    }

    private double calculatePopularityScore(Post post) {
        // Score based on upvotes, downvotes, comments, views
        int netVotes = post.upvoteCount() - post.downvoteCount();
        int engagement = post.commentCount() + post.viewCount() / 100;
        
        double score = Math.min(100, (netVotes * 2) + (engagement * 0.5));
        return Math.max(0, score);
    }

    private double calculateFreshnessScore(Post post, double timeDecayFactor) {
        Instant now = Instant.now();
        Duration age = Duration.between(post.createdAt(), now);
        double hoursOld = age.toHours();
        
        // Exponential decay: score = 100 * e^(-λt)
        // where λ is the decay rate (higher = faster decay)
        double lambda = 0.05 * timeDecayFactor; // Default half-life ~14 hours
        double score = 100 * Math.exp(-lambda * hoursOld);
        
        return Math.max(0, Math.min(100, score));
    }

    private double calculateSubredditAffinity(Post post, UserInteractionData interactionData) {
        Long subredditId = post.subreddit().id();
        
        if (interactionData.subscribedSubredditIds().contains(subredditId)) {
            return 100.0; // Subscribed subreddit - high affinity
        }
        
        // Check if user has been active in this subreddit
        Integer activityScore = interactionData.subredditActivityScore().get(subredditId);
        if (activityScore != null) {
            return Math.min(100, activityScore * 10.0); // 10 points per post
        }
        
        return 30.0; // Base score for new subreddits
    }

    private double calculateContentTypeScore(Post post, UserInteractionData interactionData) {
        if (interactionData.preferredPostTypes().isEmpty()) {
            return 50.0; // Neutral if no preference data
        }
        
        if (interactionData.preferredPostTypes().contains(post.postType())) {
            return 80.0 + (20.0 * Math.random()); // Boost for preferred types
        }
        
        return 40.0; // Slightly lower for non-preferred types
    }

    private double calculateAuthorScore(User author) {
        // Score based on author's karma and verification
        int karma = author.karma();
        double baseScore = Math.min(100, karma / 100.0); // 1 point per 100 karma
        
        if (author.isVerified()) {
            baseScore += 20;
        }
        
        return Math.min(100, baseScore);
    }

    private List<String> generateScoreReasons(Post post, UserInteractionData interactionData, double score) {
        List<String> reasons = new ArrayList<>();
        
        if (interactionData.subscribedSubredditIds().contains(post.subreddit().id())) {
            reasons.add("From your subscribed community");
        }
        
        if (post.upvoteCount() > 100) {
            reasons.add("Popular post");
        }
        
        if (post.commentCount() > 50) {
            reasons.add("Trending discussion");
        }
        
        Duration age = Duration.between(post.createdAt(), Instant.now());
        if (age.toHours() < 6) {
            reasons.add("Fresh content");
        }
        
        if (interactionData.preferredPostTypes().contains(post.postType())) {
            reasons.add("Matches your content preferences");
        }
        
        if (post.author().isVerified()) {
            reasons.add("From verified user");
        }
        
        return reasons;
    }

    private List<FeedResponse.SuggestedUser> findSuggestedUsers(User user, UserInteractionData interactionData, FeedRequest request) {
        // Find users who post in similar subreddits
        Set<Long> similarUsers = new HashSet<>();
        
        for (Long subredditId : interactionData.subredditActivityScore().keySet()) {
            subredditRepository.findById(subredditId).ifPresent(sub -> {
                List<Post> postsInSubreddit = postRepository.findBySubreddit(sub, PageRequest.of(0, 20)).getContent();
                postsInSubreddit.forEach(post -> {
                    if (!post.author().id().equals(user.id())) {
                        similarUsers.add(post.author().id());
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
        int mutualSubreddits = 0;
        List<String> commonInterests = new ArrayList<>();
        
        // Check mutual subreddits
        Set<Subreddit> theirSubreddits = subredditRepository.findSubscribedSubredditsByUser(suggestedUser.id());
        for (Subreddit sub : theirSubreddits) {
            if (interactionData.subscribedSubreddits().contains(sub)) {
                mutualSubreddits++;
                commonInterests.add(sub.name());
            }
        }
        
        if (mutualSubreddits > 0) {
            score += mutualSubreddits * 20.0;
            reasons.add("Active in " + mutualSubreddits + " communities you follow");
        }
        
        // Author reputation
        if (suggestedUser.karma() > 1000) {
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
            suggestedUser.id(),
            suggestedUser.username(),
            suggestedUser.displayName(),
            suggestedUser.bio(),
            suggestedUser.avatarUrl(),
            suggestedUser.karma(),
            suggestedUser.isVerified(),
            userType,
            Math.min(100, score),
            reasons,
            mutualSubreddits,
            commonInterests.stream().limit(3).toList()
        );
    }

    private FeedResponse.UserType determineUserType(User user) {
        if (user.karma() > 10000) {
            return FeedResponse.UserType.INFLUENCER;
        } else if (user.isVerified()) {
            return FeedResponse.UserType.VERIFIED;
        } else {
            return FeedResponse.UserType.REGULAR;
        }
    }

    private FeedResponse.FeedPost convertToFeedPost(Post post, double score, List<String> reasons, UserInteractionData interactionData) {
        FeedResponse.PostUserInfo authorInfo = new FeedResponse.PostUserInfo(
            post.author().id(),
            post.author().username(),
            post.author().displayName(),
            post.author().avatarUrl(),
            post.author().karma(),
            post.author().isVerified(),
            determineUserType(post.author())
        );
        
        FeedResponse.SubredditInfo subredditInfo = new FeedResponse.SubredditInfo(
            post.subreddit().id(),
            post.subreddit().name(),
            post.subreddit().title(),
            post.subreddit().iconImageUrl(),
            interactionData.subscribedSubredditIds().contains(post.subreddit().id())
        );
        
        FeedResponse.UserInteraction userInteraction = new FeedResponse.UserInteraction(
            interactionData.upvotedPostIds().contains(post.id()),
            interactionData.downvotedPostIds().contains(post.id()),
            false, // hasCommented - would need comment repository
            false, // hasViewed - would need view tracking
            false, // isSaved
            false, // isHidden
            0,
            null
        );
        
        return new FeedResponse.FeedPost(
            post.id(),
            post.title(),
            post.content(),
            post.url(),
            post.postType(),
            post.thumbnailUrl(),
            post.flairText(),
            post.isSpoiler(),
            post.isOver18(),
            post.score(),
            post.upvoteCount(),
            post.downvoteCount(),
            post.commentCount(),
            post.viewCount(),
            score,
            reasons,
            authorInfo,
            subredditInfo,
            post.createdAt(),
            userInteraction
        );
    }

    // Record to hold user interaction data for scoring
    private record UserInteractionData(
        Set<Long> upvotedPostIds,
        Set<Long> downvotedPostIds,
        Set<Long> subscribedSubredditIds,
        Set<Subreddit> subscribedSubreddits,
        Set<Post.PostType> preferredPostTypes,
        Map<Long, Integer> subredditActivityScore,
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
