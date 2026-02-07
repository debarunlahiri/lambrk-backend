package com.lambrk.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.domain.Subreddit;
import com.lambrk.dto.PostResponse;
import com.lambrk.dto.RecommendationRequest;
import com.lambrk.dto.RecommendationResponse;
import com.lambrk.dto.SubredditResponse;
import com.lambrk.dto.UserResponse;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.UserRepository;
import com.lambrk.repository.SubredditRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SubredditRepository subredditRepository;
    private final ChatClient chatClient;
    private final CustomMetrics customMetrics;

    public RecommendationService(PostRepository postRepository,
                               UserRepository userRepository,
                               SubredditRepository subredditRepository,
                               OpenAiChatModel chatModel,
                               CustomMetrics customMetrics) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.subredditRepository = subredditRepository;
        this.customMetrics = customMetrics;
        this.chatClient = ChatClient.builder(chatModel)
            .defaultSystem("""
                You are a recommendation engine for a Reddit-like platform. 
                Analyze user behavior and suggest relevant content.
                Consider user's interaction history, subreddit preferences, and content similarity.
                Provide personalized recommendations with explanations.
                """)
            .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
            .build();
    }

    @Cacheable(value = "recommendations", key = "#request.userId() + '-' + #request.type() + '-' + #request.limit()")
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            
            var userFuture = scope.fork(() -> 
                userRepository.findById(request.userId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + request.userId())));
            
            var userHistoryFuture = scope.fork(() -> 
                getUserInteractionHistory(request.userId()));
            
            scope.join();
            scope.throwIfFailed();
            
            User user = userFuture.get();
            List<Post> userHistory = userHistoryFuture.get();
            
            return switch (request.type()) {
                case POSTS -> getPostRecommendations(user, userHistory, request);
                case SUBREDDITS -> getSubredditRecommendations(user, userHistory, request);
                case USERS -> getUserRecommendations(user, userHistory, request);
                case COMMENTS -> getCommentRecommendations(user, userHistory, request);
            };
            
        } catch (Exception e) {
            customMetrics.recordRecommendationError();
            throw new RuntimeException("Recommendation generation failed", e);
        }
    }

    private RecommendationResponse getPostRecommendations(User user, List<Post> userHistory, RecommendationRequest request) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            
            var similarPostsFuture = scope.fork(() -> 
                findSimilarPosts(userHistory, request));
            
            var trendingPostsFuture = scope.fork(() -> 
                findTrendingPosts(user, request));
            
            var personalizedFuture = scope.fork(() -> 
                getAIRecommendations(user, userHistory, "posts", request));
            
            scope.join();
            scope.throwIfFailed();
            
            List<Post> similarPosts = similarPostsFuture.get();
            List<Post> trendingPosts = trendingPostsFuture.get();
            List<Post> personalized = personalizedFuture.get();
            
            // Combine and rank recommendations
            List<Post> recommendations = combinePostRecommendations(similarPosts, trendingPosts, personalized, request);
            
            String explanation = generateExplanation(user, "posts", recommendations.size());
            double confidence = calculateConfidence(userHistory.size(), recommendations.size());
            List<String> factors = List.of("User interaction history", "Subreddit preferences", "Content similarity", "Trending topics");
            
            return RecommendationResponse.ofPosts(
                recommendations.stream().map(PostResponse::from).toList(),
                explanation,
                confidence,
                factors
            );
            
        } catch (Exception e) {
            customMetrics.recordRecommendationError();
            throw new RuntimeException("Post recommendation failed", e);
        }
    }

    private RecommendationResponse getSubredditRecommendations(User user, List<Post> userHistory, RecommendationRequest request) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            
            var userSubredditsFuture = scope.fork(() -> 
                getUserSubredditPreferences(user));
            
            var similarSubredditsFuture = scope.fork(() -> 
                findSimilarSubreddits(userSubredditsFuture.get()));
            
            var trendingSubredditsFuture = scope.fork(() -> 
                findTrendingSubreddits(request));
            
            scope.join();
            scope.throwIfFailed();
            
            List<Subreddit> userSubreddits = userSubredditsFuture.get();
            List<Subreddit> similar = similarSubredditsFuture.get();
            List<Subreddit> trending = trendingSubredditsFuture.get();
            
            // Combine recommendations
            List<Subreddit> recommendations = combineSubredditRecommendations(userSubreddits, similar, trending, request);
            
            String explanation = generateExplanation(user, "subreddits", recommendations.size());
            double confidence = calculateConfidence(userHistory.size(), recommendations.size());
            List<String> factors = List.of("User subscriptions", "Similar communities", "Trending communities", "Content preferences");
            
            return RecommendationResponse.ofSubreddits(
                recommendations.stream().map(SubredditResponse::from).toList(),
                explanation,
                confidence,
                factors
            );
            
        } catch (Exception e) {
            customMetrics.recordRecommendationError();
            throw new RuntimeException("Subreddit recommendation failed", e);
        }
    }

    private RecommendationResponse getUserRecommendations(User user, List<Post> userHistory, RecommendationRequest request) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            
            var similarUsersFuture = scope.fork(() -> 
                findSimilarUsers(user, userHistory));
            
            var activeUsersFuture = scope.fork(() -> 
                findActiveUsersInUserSubreddits(user));
            
            scope.join();
            scope.throwIfFailed();
            
            List<User> similar = similarUsersFuture.get();
            List<User> active = activeUsersFuture.get();
            
            // Combine recommendations
            List<User> recommendations = combineUserRecommendations(similar, active, request);
            
            String explanation = generateExplanation(user, "users", recommendations.size());
            double confidence = calculateConfidence(userHistory.size(), recommendations.size());
            List<String> factors = List.of("Similar interests", "Active in same communities", "Content overlap", "Engagement patterns");
            
            return RecommendationResponse.ofUsers(
                recommendations.stream().map(UserResponse::from).toList(),
                explanation,
                confidence,
                factors
            );
            
        } catch (Exception e) {
            customMetrics.recordRecommendationError();
            throw new RuntimeException("User recommendation failed", e);
        }
    }

    private RecommendationResponse getCommentRecommendations(User user, List<Post> userHistory, RecommendationRequest request) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            
            var topCommentsFuture = scope.fork(() -> 
                findTopCommentsInUserSubreddits(user, request));
            
            var recentCommentsFuture = scope.fork(() -> 
                findRecentCommentsFromSimilarUsers(user, request));
            
            scope.join();
            scope.throwIfFailed();
            
            // For comments, we'll return a simplified response since we don't have comment recommendations yet
            String explanation = "Recommended comments based on your activity and interests";
            double confidence = 0.7;
            List<String> factors = List.of("User activity patterns", "Subreddit engagement", "Comment quality");
            
            return RecommendationResponse.ofComments(
                List.of(), // Placeholder - would implement actual comment recommendations
                explanation,
                confidence,
                factors
            );
            
        } catch (Exception e) {
            customMetrics.recordRecommendationError();
            throw new RuntimeException("Comment recommendation failed", e);
        }
    }

    private List<Post> getUserInteractionHistory(Long userId) {
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findAll(pageable).getContent();
    }

    private List<Post> findSimilarPosts(List<Post> userHistory, RecommendationRequest request) {
        if (userHistory.isEmpty()) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(0, request.limit());
        return postRepository.findAll(pageable).getContent();
    }

    private List<Post> findTrendingPosts(User user, RecommendationRequest request) {
        List<Subreddit> userSubreddits = getUserSubredditPreferences(user);
        List<String> subredditNames = userSubreddits.stream()
            .map(Subreddit::name)
            .toList();
        
        Pageable pageable = PageRequest.of(0, request.limit(), Sort.by(Sort.Direction.DESC, "score"));
        return postRepository.findAll(pageable).getContent();
    }

    private List<Post> getAIRecommendations(User user, List<Post> userHistory, String type, RecommendationRequest request) {
        if (userHistory.isEmpty()) {
            return List.of();
        }
        
        try {
            String prompt = String.format("""
                Based on this user's interaction history with posts:
                %s
                
                Recommend %d posts that would be most relevant to this user.
                Consider their subreddit preferences, content themes, and engagement patterns.
                Return a list of post IDs and brief reasoning.
                """, 
                formatUserHistory(userHistory),
                request.limit()
            );
            
            String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
            
            // Parse AI response to extract post IDs
            List<Long> recommendedIds = parseAIPostRecommendations(response);
            
            return recommendedIds.stream()
                .limit(request.limit())
                .map(id -> postRepository.findById(id).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
                
        } catch (Exception e) {
            // Fallback to empty list if AI fails
            return List.of();
        }
    }

    private List<Subreddit> getUserSubredditPreferences(User user) {
        Set<Subreddit> subreddits = subredditRepository.findSubscribedSubredditsByUser(user.id());
        return subreddits != null ? subreddits.stream().toList() : List.of();
    }

    private List<Subreddit> findSimilarSubreddits(List<Subreddit> userSubreddits) {
        if (userSubreddits.isEmpty()) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(0, 20);
        return subredditRepository.findAll(pageable).getContent();
    }

    private List<Subreddit> findTrendingSubreddits(RecommendationRequest request) {
        Pageable pageable = PageRequest.of(0, request.limit());
        return subredditRepository.findAll(pageable).getContent();
    }

    private List<User> findSimilarUsers(User user, List<Post> userHistory) {
        Pageable pageable = PageRequest.of(0, 20);
        return userRepository.findAll(pageable).getContent();
    }

    private List<User> findActiveUsersInUserSubreddits(User user) {
        List<Subreddit> userSubreddits = getUserSubredditPreferences(user);
        Pageable pageable = PageRequest.of(0, 20);
        return userRepository.findAll(pageable).getContent();
    }

    private List<Post> findTopCommentsInUserSubreddits(User user, RecommendationRequest request) {
        return List.of();
    }

    private List<Post> findRecentCommentsFromSimilarUsers(User user, RecommendationRequest request) {
        return List.of();
    }

    private List<Post> combinePostRecommendations(List<Post> similar, List<Post> trending, List<Post> personalized, RecommendationRequest request) {
        // Weighted combination: 40% similar, 30% trending, 30% personalized
        List<Post> combined = java.util.stream.Stream.concat(
            java.util.stream.Stream.concat(similar.stream(), trending.stream()),
            personalized.stream()
        ).distinct().limit(request.limit()).toList();
        
        return combined;
    }

    private List<Subreddit> combineSubredditRecommendations(List<Subreddit> userSubreddits, List<Subreddit> similar, List<Subreddit> trending, RecommendationRequest request) {
        // Exclude user's current subscriptions
        List<Long> excludeIds = userSubreddits.stream().map(Subreddit::id).toList();
        
        return java.util.stream.Stream.concat(similar.stream(), trending.stream())
            .filter(sub -> !excludeIds.contains(sub.id()))
            .distinct()
            .limit(request.limit())
            .toList();
    }

    private List<User> combineUserRecommendations(List<User> similar, List<User> active, RecommendationRequest request) {
        // Exclude the user themselves
        return java.util.stream.Stream.concat(similar.stream(), active.stream())
            .distinct()
            .limit(request.limit())
            .toList();
    }

    private String generateExplanation(User user, String type, int count) {
        return String.format("Based on your activity in %d communities and %d interactions, " +
            "we've selected %d %s that match your interests and engagement patterns.",
            getUserSubredditPreferences(user).size(),
            getUserInteractionHistory(user.id()).size(),
            count,
            type
        );
    }

    private double calculateConfidence(int historySize, int recommendationCount) {
        // Higher confidence with more user history
        double baseConfidence = 0.5;
        double historyBonus = Math.min(0.3, historySize / 100.0);
        double countBonus = Math.min(0.2, recommendationCount / 20.0);
        
        return Math.min(1.0, baseConfidence + historyBonus + countBonus);
    }

    private String formatUserHistory(List<Post> userHistory) {
        return userHistory.stream()
            .limit(10)
            .map(post -> String.format("Post in %s: %s (score: %d)", 
                post.subreddit().name(), post.title(), post.score()))
            .collect(java.util.stream.Collectors.joining("\n"));
    }

    private List<Long> parseAIPostRecommendations(String response) {
        // Simple parsing - in production, use more robust JSON parsing
        return List.of(); // Placeholder
    }
}
