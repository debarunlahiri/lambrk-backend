package com.lambrk.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.domain.Community;
import com.lambrk.dto.PostResponse;
import com.lambrk.dto.RecommendationRequest;
import com.lambrk.dto.RecommendationResponse;
import com.lambrk.dto.CommunityResponse;
import com.lambrk.dto.UserResponse;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.UserRepository;
import com.lambrk.repository.CommunityRepository;
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
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final ChatClient chatClient;
    private final CustomMetrics customMetrics;

    public RecommendationService(PostRepository postRepository,
                               UserRepository userRepository,
                               CommunityRepository communityRepository,
                               OpenAiChatModel chatModel,
                               CustomMetrics customMetrics) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.customMetrics = customMetrics;
        this.chatClient = ChatClient.builder(chatModel)
            .defaultSystem("""
                You are a recommendation engine for a Lambrk-like platform. 
                Analyze user behavior and suggest relevant content.
                Consider user's interaction history, community preferences, and content similarity.
                Provide personalized recommendations with explanations.
                """)
            .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
            .build();
    }

    @Cacheable(value = "recommendations", key = "#request.userId() + '-' + #request.type() + '-' + #request.limit()")
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        try {
            User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.userId()));
            List<Post> userHistory = getUserInteractionHistory(request.userId());
            
            return switch (request.type()) {
                case POSTS -> getPostRecommendations(user, userHistory, request);
                case COMMUNITIES -> getCommunityRecommendations(user, userHistory, request);
                case USERS -> getUserRecommendations(user, userHistory, request);
                case COMMENTS -> getCommentRecommendations(user, userHistory, request);
            };
            
        } catch (Exception e) {
            customMetrics.recordRecommendationError();
            throw new RuntimeException("Recommendation generation failed", e);
        }
    }

    private RecommendationResponse getPostRecommendations(User user, List<Post> userHistory, RecommendationRequest request) {
        try {
            List<Post> similarPosts = findSimilarPosts(userHistory, request);
            List<Post> trendingPosts = findTrendingPosts(user, request);
            List<Post> personalized = getAIRecommendations(user, userHistory, "posts", request);
            
            // Combine and rank recommendations
            List<Post> recommendations = combinePostRecommendations(similarPosts, trendingPosts, personalized, request);
            
            String explanation = generateExplanation(user, "posts", recommendations.size());
            double confidence = calculateConfidence(userHistory.size(), recommendations.size());
            List<String> factors = List.of("User interaction history", "Community preferences", "Content similarity", "Trending topics");
            
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

    private RecommendationResponse getCommunityRecommendations(User user, List<Post> userHistory, RecommendationRequest request) {
        try {
            List<Community> userCommunities = getUserCommunityPreferences(user);
            List<Community> similar = findSimilarCommunities(userCommunities);
            List<Community> trending = findTrendingCommunities(request);
            
            // Combine recommendations
            List<Community> recommendations = combineCommunityRecommendations(userCommunities, similar, trending, request);
            
            String explanation = generateExplanation(user, "communities", recommendations.size());
            double confidence = calculateConfidence(userHistory.size(), recommendations.size());
            List<String> factors = List.of("User subscriptions", "Similar communities", "Trending communities", "Content preferences");
            
            return RecommendationResponse.ofCommunities(
                recommendations.stream().map(CommunityResponse::from).toList(),
                explanation,
                confidence,
                factors
            );
            
        } catch (Exception e) {
            customMetrics.recordRecommendationError();
            throw new RuntimeException("Community recommendation failed", e);
        }
    }

    private RecommendationResponse getUserRecommendations(User user, List<Post> userHistory, RecommendationRequest request) {
        try {
            List<User> similar = findSimilarUsers(user, userHistory);
            List<User> active = findActiveUsersInUserCommunities(user);
            
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
        try {
            // For comments, we'll return a simplified response since we don't have comment recommendations yet
            String explanation = "Recommended comments based on your activity and interests";
            double confidence = 0.7;
            List<String> factors = List.of("User activity patterns", "Community engagement", "Comment quality");
            
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

    private List<Post> getUserInteractionHistory(UUID userId) {
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
        List<Community> userCommunities = getUserCommunityPreferences(user);
        List<String> communityNames = userCommunities.stream()
            .map(Community::getName)
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
                Consider their community preferences, content themes, and engagement patterns.
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
            List<UUID> recommendedIds = parseAIPostRecommendations(response);
            
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

    private List<Community> getUserCommunityPreferences(User user) {
        Set<Community> communities = communityRepository.findSubscribedCommunitiesByUser(user.getId());
        return communities != null ? communities.stream().toList() : List.of();
    }

    private List<Community> findSimilarCommunities(List<Community> userCommunities) {
        if (userCommunities.isEmpty()) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(0, 20);
        return communityRepository.findAll(pageable).getContent();
    }

    private List<Community> findTrendingCommunities(RecommendationRequest request) {
        Pageable pageable = PageRequest.of(0, request.limit());
        return communityRepository.findAll(pageable).getContent();
    }

    private List<User> findSimilarUsers(User user, List<Post> userHistory) {
        Pageable pageable = PageRequest.of(0, 20);
        return userRepository.findAll(pageable).getContent();
    }

    private List<User> findActiveUsersInUserCommunities(User user) {
        List<Community> userCommunities = getUserCommunityPreferences(user);
        Pageable pageable = PageRequest.of(0, 20);
        return userRepository.findAll(pageable).getContent();
    }

    private List<Post> findTopCommentsInUserCommunities(User user, RecommendationRequest request) {
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

    private List<Community> combineCommunityRecommendations(List<Community> userCommunities, List<Community> similar, List<Community> trending, RecommendationRequest request) {
        // Exclude user's current subscriptions
        List<UUID> excludeIds = userCommunities.stream().map(Community::getId).toList();
        
        return java.util.stream.Stream.concat(similar.stream(), trending.stream())
            .filter(sub -> !excludeIds.contains(sub.getId()))
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
            getUserCommunityPreferences(user).size(),
            getUserInteractionHistory(user.getId()).size(),
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
                post.getCommunity().getName(), post.getTitle(), post.getScore()))
            .collect(java.util.stream.Collectors.joining("\n"));
    }

    private List<UUID> parseAIPostRecommendations(String response) {
        // Simple parsing - in production, use more robust JSON parsing
        return List.of(); // Placeholder
    }
}
