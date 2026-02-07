package com.lambrk.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.StructuredTaskScope;

@Service
public class AIContentModerationService {

    private final ChatClient chatClient;
    private final CustomMetrics customMetrics;

    public AIContentModerationService(OpenAiChatModel chatModel, CustomMetrics customMetrics) {
        this.customMetrics = customMetrics;
        this.chatClient = ChatClient.builder(chatModel)
            .defaultSystem("""
                You are a content moderator for a Reddit-like platform. Your task is to analyze content 
                for policy violations. Respond with a JSON object containing:
                - "approved": boolean (true if content is acceptable)
                - "reason": string (explanation for decision)
                - "categories": array of strings (policy categories if violated)
                - "confidence": number (0.0 to 1.0)
                
                Policy categories include: hate_speech, harassment, violence, spam, nsfw, misinformation.
                Be thorough but fair. Allow controversial but legitimate discussions.
                """)
            .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
            .build();
    }

    @Cacheable(value = "contentModeration", key = "#content.hashCode()")
    public ModerationResult moderateContent(String content, String contentType) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            
            var moderationFuture = scope.fork(() -> performModeration(content, contentType));
            var toxicityFuture = scope.fork(() -> analyzeToxicity(content));
            var spamFuture = scope.fork(() -> analyzeSpam(content));
            
            scope.join();
            scope.throwIfFailed();
            
            ModerationResult moderation = moderationFuture.get();
            ToxicityAnalysis toxicity = toxicityFuture.get();
            SpamAnalysis spam = spamFuture.get();
            
            // Combine results
            boolean approved = moderation.approved() && 
                             toxicity.toxicityScore() < 0.8 && 
                             spam.spamScore() < 0.7;
            
            return new ModerationResult(
                approved,
                moderation.reason(),
                moderation.categories(),
                Math.max(moderation.confidence(), Math.max(toxicity.toxicityScore(), spam.spamScore())),
                toxicity,
                spam
            );
        } catch (Exception e) {
            customMetrics.recordModerationError(contentType);
            throw new RuntimeException("Content moderation failed", e);
        }
    }

    private ModerationResult performModeration(String content, String contentType) {
        String prompt = String.format("""
            Analyze this %s for policy violations:
            
            Content: "%s"
            
            Provide moderation decision in the specified JSON format.
            """, contentType, content);
        
        String response = chatClient.prompt()
            .user(prompt)
            .call()
            .content();
        
        // Parse JSON response (simplified - in production use proper JSON parsing)
        boolean approved = response.contains("\"approved\": true");
        double confidence = extractConfidence(response);
        List<String> categories = extractCategories(response);
        String reason = extractReason(response);
        
        customMetrics.recordModeration(contentType, approved);
        
        return new ModerationResult(approved, reason, categories, confidence, null, null);
    }

    private ToxicityAnalysis analyzeToxicity(String content) {
        String prompt = String.format("""
            Analyze the toxicity level of this content on a scale of 0.0 to 1.0.
            Also identify specific toxic behaviors (profanity, hate speech, personal attacks, etc.).
            
            Content: "%s"
            
            Respond with JSON: {"toxicityScore": 0.0, "behaviors": ["list"], "explanation": "..."}
            """, content);
        
        String response = chatClient.prompt()
            .user(prompt)
            .call()
            .content();
        
        double toxicityScore = extractToxicityScore(response);
        List<String> behaviors = extractToxicBehaviors(response);
        String explanation = extractExplanation(response);
        
        return new ToxicityAnalysis(toxicityScore, behaviors, explanation);
    }

    private SpamAnalysis analyzeSpam(String content) {
        String prompt = String.format("""
            Analyze this content for spam characteristics on a scale of 0.0 to 1.0.
            Look for repetitive content, promotional language, suspicious links, etc.
            
            Content: "%s"
            
            Respond with JSON: {"spamScore": 0.0, "indicators": ["list"], "explanation": "..."}
            """, content);
        
        String response = chatClient.prompt()
            .user(prompt)
            .call()
            .content();
        
        double spamScore = extractSpamScore(response);
        List<String> indicators = extractSpamIndicators(response);
        String explanation = extractExplanation(response);
        
        return new SpamAnalysis(spamScore, indicators, explanation);
    }

    @Cacheable(value = "contentRecommendations", key = "#userId + '-' + #limit")
    public List<Recommendation> getPersonalizedRecommendations(Long userId, int limit) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            
            var userHistoryFuture = scope.fork(() -> getUserContentHistory(userId));
            var trendingFuture = scope.fork(() -> getTrendingContent());
            var similarUsersFuture = scope.fork(() -> getSimilarUsersContent(userId));
            
            scope.join();
            scope.throwIfFailed();
            
            // Combine and rank recommendations
            return combineRecommendations(
                userHistoryFuture.get(),
                trendingFuture.get(),
                similarUsersFuture.get(),
                limit
            );
        } catch (Exception e) {
            customMetrics.recordRecommendationError();
            throw new RuntimeException("Recommendation generation failed", e);
        }
    }

    private List<Recommendation> getUserContentHistory(Long userId) {
        // This would query user's interaction history
        // For now, return empty list
        return List.of();
    }

    private List<Recommendation> getTrendingContent() {
        // This would get trending posts based on engagement metrics
        return List.of();
    }

    private List<Recommendation> getSimilarUsersContent(Long userId) {
        // This would find similar users and their preferred content
        return List.of();
    }

    private List<Recommendation> combineRecommendations(
            List<Recommendation> userHistory,
            List<Recommendation> trending,
            List<Recommendation> similarUsers,
            int limit) {
        
        // Implement recommendation algorithm
        // Combine different sources with weights and rank
        return List.of(); // Placeholder
    }

    // Helper methods for parsing AI responses
    private double extractConfidence(String response) {
        // Extract confidence value from JSON response
        return 0.8; // Placeholder
    }

    private List<String> extractCategories(String response) {
        // Extract categories from JSON response
        return List.of(); // Placeholder
    }

    private String extractReason(String response) {
        // Extract reason from JSON response
        return "Content analysis completed"; // Placeholder
    }

    private double extractToxicityScore(String response) {
        return 0.2; // Placeholder
    }

    private List<String> extractToxicBehaviors(String response) {
        return List.of(); // Placeholder
    }

    private double extractSpamScore(String response) {
        return 0.1; // Placeholder
    }

    private List<String> extractSpamIndicators(String response) {
        return List.of(); // Placeholder
    }

    private String extractExplanation(String response) {
        return "Analysis completed"; // Placeholder
    }

    public record ModerationResult(
        boolean approved,
        String reason,
        List<String> categories,
        double confidence,
        ToxicityAnalysis toxicity,
        SpamAnalysis spam
    ) {}

    public record ToxicityAnalysis(
        double toxicityScore,
        List<String> behaviors,
        String explanation
    ) {}

    public record SpamAnalysis(
        double spamScore,
        List<String> indicators,
        String explanation
    ) {}

    public record Recommendation(
        Long contentId,
        String contentType,
        String title,
        double score,
        String reason
    ) {}
}
