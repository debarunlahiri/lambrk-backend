package com.lambrk.service;

import com.lambrk.domain.Community;
import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.domain.Vote;
import com.lambrk.repository.CommentRepository;
import com.lambrk.repository.CommunityRepository;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.UserRepository;
import com.lambrk.repository.VoteRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Profile("!test")
public class AIContentModerationService {

  private static final Logger logger = LoggerFactory.getLogger(AIContentModerationService.class);

  private final ChatClient chatClient;
  private final CustomMetrics customMetrics;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final VoteRepository voteRepository;
  private final UserRepository userRepository;
  private final CommunityRepository communityRepository;

  public AIContentModerationService(
      OpenAiChatModel chatModel,
      CustomMetrics customMetrics,
      PostRepository postRepository,
      CommentRepository commentRepository,
      VoteRepository voteRepository,
      UserRepository userRepository,
      CommunityRepository communityRepository) {
    this.customMetrics = customMetrics;
    this.postRepository = postRepository;
    this.commentRepository = commentRepository;
    this.voteRepository = voteRepository;
    this.userRepository = userRepository;
    this.communityRepository = communityRepository;

    this.chatClient =
        ChatClient.builder(chatModel)
            .defaultSystem(
                """
You are a content moderator for a Lambrk-like platform. Your task is to analyze content
for policy violations. Respond with a JSON object containing:
- "approved": boolean (true if content is acceptable)
- "reason": string (explanation for decision)
- "categories": array of strings (policy categories if violated)
- "confidence": number (0.0 to 1.0)

Policy categories include: hate_speech, harassment, violence, spam, nsfw, misinformation, adult_content.
Be thorough but fair. Allow controversial but legitimate discussions.
""")
            .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
            .build();
  }

  @Cacheable(value = "contentModeration", key = "#content.hashCode() + '-' + #contentType")
  public ModerationResult moderateContent(String content, String contentType) {
    try {
      ModerationResult moderation = performModeration(content, contentType);
      ToxicityAnalysis toxicity = analyzeToxicity(content);
      SpamAnalysis spam = analyzeSpam(content);

      boolean approved =
          moderation.approved() && toxicity.toxicityScore() < 0.8 && spam.spamScore() < 0.7;

      double finalConfidence =
          Math.max(moderation.confidence(), Math.max(toxicity.toxicityScore(), spam.spamScore()));

      List<String> allCategories = new ArrayList<>(moderation.categories());
      if (toxicity.toxicityScore() >= 0.6) {
        allCategories.add("toxic_content");
      }
      if (spam.spamScore() >= 0.5) {
        allCategories.add("spam_indicators");
      }

      return new ModerationResult(
          approved, moderation.reason(), allCategories, finalConfidence, toxicity, spam);
    } catch (Exception e) {
      logger.error("Content moderation failed for type: {}", contentType, e);
      customMetrics.recordModerationError(contentType);
      throw new RuntimeException("Content moderation failed", e);
    }
  }

  private ModerationResult performModeration(String content, String contentType) {
    String prompt =
        String.format(
            """
Analyze this %s for policy violations.

Content: "%s"

Provide moderation decision as JSON with keys: approved, reason, categories (array), confidence (0.0-1.0)
""",
            contentType, content.substring(0, Math.min(content.length(), 2000)));

    String response = chatClient.prompt().user(prompt).call().content();

    boolean approved = extractBoolean(response, "approved", true);
    double confidence = extractDouble(response, "confidence", 0.5);
    List<String> categories = extractCategories(response);
    String reason = extractReason(response);

    customMetrics.recordModeration(contentType, approved);

    return new ModerationResult(approved, reason, categories, confidence, null, null);
  }

  private ToxicityAnalysis analyzeToxicity(String content) {
    String prompt =
        String.format(
            """
Analyze the toxicity level of this content on a scale of 0.0 to 1.0.
Also identify specific toxic behaviors (profanity, hate speech, personal attacks, etc.).

Content: "%s"

Provide JSON: {"toxicityScore": 0.0-1.0, "behaviors": ["list"], "explanation": "..."}
""",
            content.substring(0, Math.min(content.length(), 1500)));

    String response = chatClient.prompt().user(prompt).call().content();

    double toxicityScore = extractDouble(response, "toxicityScore", 0.2);
    List<String> behaviors = extractStringList(response, "behaviors");
    String explanation = extractStringValue(response, "explanation", "Analysis completed");

    return new ToxicityAnalysis(toxicityScore, behaviors, explanation);
  }

  private SpamAnalysis analyzeSpam(String content) {
    String prompt =
        String.format(
            """
            Analyze this content for spam characteristics on a scale of 0.0 to 1.0.
            Look for repetitive content, promotional language, suspicious links, etc.

            Content: "%s"

            Provide JSON: {"spamScore": 0.0-1.0, "indicators": ["list"], "explanation": "..."}
            """,
            content.substring(0, Math.min(content.length(), 1500)));

    String response = chatClient.prompt().user(prompt).call().content();

    double spamScore = extractDouble(response, "spamScore", 0.1);
    List<String> indicators = extractStringList(response, "indicators");
    String explanation = extractStringValue(response, "explanation", "Analysis completed");

    return new SpamAnalysis(spamScore, indicators, explanation);
  }

  @Cacheable(value = "contentRecommendations", key = "#userId + '-' + #limit")
  public List<Recommendation> getPersonalizedRecommendations(UUID userId, int limit) {
    try {
      List<Recommendation> userHistoryRecs = getUserContentHistory(userId, limit);
      List<Recommendation> trendingRecs = getTrendingContent(limit);
      List<Recommendation> similarUsersRecs = getSimilarUsersContent(userId, limit);
      List<Recommendation> subscribedRecs = getSubscribedCommunitiesContent(userId, limit);

      return combineRecommendations(
          userHistoryRecs, trendingRecs, similarUsersRecs, subscribedRecs, limit);
    } catch (Exception e) {
      logger.error("Recommendation generation failed for user: {}", userId, e);
      customMetrics.recordRecommendationError();
      throw new RuntimeException("Recommendation generation failed", e);
    }
  }

  private List<Recommendation> getUserContentHistory(UUID userId, int limit) {
    Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
    Pageable pageable = PageRequest.of(0, limit);

    List<Recommendation> recommendations = new ArrayList<>();

    Page<Post> userPosts = postRepository.findUserPostsSince(userId, thirtyDaysAgo, pageable);
    List<String> interactedCommunities = getUserInteractedCommunities(userId);

    for (Post post : userPosts.getContent()) {
      Community community = post.getCommunity();
      if (!interactedCommunities.contains(community.getName())) {
        recommendations.add(
            new Recommendation(
                community.getId(),
                "community",
                community.getName(),
                0.6,
                "Related to your activity"));
      }
    }

    List<Vote> userVotes = voteRepository.findPostVotesByUser(userId);
    for (Vote vote : userVotes.stream().limit(20).toList()) {
      if (vote.getPost() != null) {
        Post post = vote.getPost();
        for (Post similarPost : findSimilarPosts(post, 3)) {
          recommendations.add(
              new Recommendation(
                  similarPost.getId(),
                  "post",
                  similarPost.getTitle(),
                  0.7,
                  "Similar to posts you've liked"));
        }
      }
    }

    return recommendations.stream().limit(limit).toList();
  }

  private List<String> getUserInteractedCommunities(UUID userId) {
    Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
    Pageable pageable = PageRequest.of(0, 50);

    Page<Post> posts = postRepository.findUserPostsSince(userId, thirtyDaysAgo, pageable);
    return posts.getContent().stream()
        .map(post -> post.getCommunity().getName())
        .distinct()
        .collect(Collectors.toList());
  }

  private List<Post> findSimilarPosts(Post post, int limit) {
    Pageable pageable = PageRequest.of(0, limit);

    String textToAnalyze =
        (post.getTitle() != null ? post.getTitle() : "")
            + " "
            + (post.getContent() != null ? post.getContent() : "");
    List<String> keywords = extractKeywords(textToAnalyze);

    return postRepository.findByCommunity(post.getCommunity(), pageable).getContent().stream()
        .filter(p -> !p.getId().equals(post.getId()))
        .filter(
            p ->
                keywords.stream()
                    .anyMatch(
                        k ->
                            p.getTitle().toLowerCase().contains(k.toLowerCase())
                                || (p.getContent() != null
                                    && p.getContent().toLowerCase().contains(k.toLowerCase()))))
        .toList();
  }

  private List<String> extractKeywords(String text) {
    String[] words = text.toLowerCase().split("\\s+");
    return Arrays.stream(words)
        .filter(word -> word.length() > 4)
        .filter(word -> !CommonWords.isCommonWord(word))
        .distinct()
        .limit(10)
        .toList();
  }

  private List<Recommendation> getTrendingContent(int limit) {
    Instant twentyFourHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS);
    Pageable pageable = PageRequest.of(0, limit);

    Page<Post> trendingPosts = postRepository.findHotPostsSince(twentyFourHoursAgo, pageable);

    return trendingPosts.getContent().stream()
        .filter(post -> !post.isOver18())
        .map(
            post ->
                new Recommendation(
                    post.getId(),
                    "post",
                    post.getTitle(),
                    calculateTrendingScore(post),
                    "Trending in your feed"))
        .toList();
  }

  private double calculateTrendingScore(Post post) {
    double timeWeight = ChronoUnit.HOURS.between(post.getCreatedAt(), Instant.now());
    timeWeight = Math.max(1, 24 - timeWeight) / 24;

    double scoreWeight = Math.min(post.getScore() / 1000.0, 1.0);
    double commentWeight = Math.min(post.getCommentCount() / 50.0, 1.0);

    return (timeWeight * 0.4 + scoreWeight * 0.4 + commentWeight * 0.2);
  }

  private List<Recommendation> getSimilarUsersContent(UUID userId, int limit) {
    User currentUser = userRepository.findById(userId).orElse(null);
    if (currentUser == null) return List.of();

    Pageable pageable = PageRequest.of(0, limit * 2);

    Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
    List<Vote> userVotes = voteRepository.findPostVotesByUser(userId);

    if (userVotes.isEmpty()) return List.of();

    Set<UUID> likedPostIds =
        userVotes.stream()
            .filter(v -> v.getVoteType() == Vote.VoteType.LIKE)
            .filter(v -> v.getPost() != null)
            .map(v -> v.getPost().getId())
            .collect(Collectors.toSet());

    if (likedPostIds.isEmpty()) return List.of();

    List<Post> posts = postRepository.findAll(pageable).getContent();
    List<Recommendation> recommendations = new ArrayList<>();

    Set<Post> likedPosts =
        userVotes.stream()
            .filter(v -> v.getPost() != null)
            .map(Vote::getPost)
            .collect(Collectors.toSet());

    for (Post post : posts) {
      if (!likedPostIds.contains(post.getId()) && !post.isArchived()) {
        long sharedCommunities =
            posts.stream()
                .filter(
                    p ->
                        likedPosts.contains(p)
                            && p.getCommunity().getId().equals(post.getCommunity().getId()))
                .count();

        if (sharedCommunities > 0) {
          recommendations.add(
              new Recommendation(
                  post.getId(),
                  "post",
                  post.getTitle(),
                  Math.min(sharedCommunities / 5.0, 0.8),
                  "Popular among similar users"));
        }
      }
    }

    return recommendations.stream().limit(limit).toList();
  }

  private List<Recommendation> getSubscribedCommunitiesContent(UUID userId, int limit) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null || user.getMemberships() == null) return List.of();

    Pageable pageable = PageRequest.of(0, limit);

    List<Recommendation> recommendations = new ArrayList<>();

    for (Community community : user.getActiveSubscribedCommunities()) {
      Page<Post> posts = postRepository.findByCommunityAndIsArchivedFalse(community, pageable);
      for (Post post : posts.getContent()) {
        if (!post.isOver18()) {
          recommendations.add(
              new Recommendation(
                  post.getId(),
                  "post",
                  post.getTitle(),
                  0.8,
                  "From your subscribed community: " + community.getName()));
        }
      }
    }

    return recommendations.stream().limit(limit).toList();
  }

  private List<Recommendation> combineRecommendations(
      List<Recommendation> userHistory,
      List<Recommendation> trending,
      List<Recommendation> similarUsers,
      List<Recommendation> subscribed,
      int limit) {

    Map<String, Recommendation> merged = new LinkedHashMap<>();
    double[] weights = {0.35, 0.25, 0.20, 0.20};
    List<List<Recommendation>> sources = List.of(userHistory, trending, similarUsers, subscribed);

    for (int i = 0; i < sources.size(); i++) {
      for (Recommendation rec : sources.get(i)) {
        String key = rec.contentId() + "-" + rec.contentType();
        if (!merged.containsKey(key)) {
          double adjustedScore = rec.score() * weights[i];
          merged.put(
              key,
              new Recommendation(
                  rec.contentId(), rec.contentType(), rec.title(), adjustedScore, rec.reason()));
        } else {
          Recommendation existing = merged.get(key);
          double newScore = existing.score() + rec.score() * weights[i] * 0.5;
          merged.put(
              key,
              new Recommendation(
                  existing.contentId(),
                  existing.contentType(),
                  existing.title(),
                  newScore,
                  existing.reason() + " | " + rec.reason()));
        }
      }
    }

    return merged.values().stream()
        .sorted(Comparator.comparing(Recommendation::score).reversed())
        .limit(limit)
        .toList();
  }

  private boolean extractBoolean(String json, String key, boolean defaultValue) {
    Pattern pattern =
        Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(json);
    if (matcher.find()) {
      return matcher.group(1).equalsIgnoreCase("true");
    }
    return defaultValue;
  }

  private double extractDouble(String json, String key, double defaultValue) {
    Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9.]+)");
    Matcher matcher = pattern.matcher(json);
    if (matcher.find()) {
      try {
        return Double.parseDouble(matcher.group(1));
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  private List<String> extractCategories(String json) {
    return extractStringList(json, "categories");
  }

  private List<String> extractStringList(String json, String key) {
    Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[([^\\]]*)\\]");
    Matcher matcher = pattern.matcher(json);
    if (matcher.find()) {
      String content = matcher.group(1);
      if (content.trim().isEmpty()) return List.of();
      return Arrays.stream(content.split(","))
          .map(s -> s.replace("\"", "").trim())
          .filter(s -> !s.isEmpty())
          .collect(java.util.stream.Collectors.toList());
    }
    return List.of();
  }

  private String extractReason(String json) {
    String extracted = extractStringValue(json, "reason", null);
    if (extracted != null) return extracted;

    Pattern pattern = Pattern.compile("\"reason\"\\s*:\\s*\"([^\"]*)\"");
    Matcher matcher = pattern.matcher(json);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "Content analysis completed";
  }

  private String extractStringValue(String json, String key, String defaultValue) {
    Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
    Matcher matcher = pattern.matcher(json);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return defaultValue;
  }

  public record ModerationResult(
      boolean approved,
      String reason,
      List<String> categories,
      double confidence,
      ToxicityAnalysis toxicity,
      SpamAnalysis spam) {}

  public record ToxicityAnalysis(
      double toxicityScore, List<String> behaviors, String explanation) {}

  public record SpamAnalysis(double spamScore, List<String> indicators, String explanation) {}

  public record Recommendation(
      UUID contentId, String contentType, String title, double score, String reason) {}

  private static class CommonWords {
    private static final Set<String> WORDS =
        Set.of(
            "about", "after", "again", "being", "could", "would", "should", "their", "there",
            "these", "thing", "think", "those", "through", "where", "which", "while", "withou",
            "your", "just", "like", "know", "make", "only", "such", "take", "than", "them", "very");

    static boolean isCommonWord(String word) {
      return WORDS.contains(word.toLowerCase());
    }
  }
}
