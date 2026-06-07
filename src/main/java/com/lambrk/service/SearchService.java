package com.lambrk.service;

import com.lambrk.domain.Comment;
import com.lambrk.domain.Community;
import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.dto.CommentResponse;
import com.lambrk.dto.CommunityResponse;
import com.lambrk.dto.PostResponse;
import com.lambrk.dto.SearchRequest;
import com.lambrk.dto.SearchResponse;
import com.lambrk.dto.SocialUserResponse;
import com.lambrk.repository.CommentRepository;
import com.lambrk.repository.CommunityRepository;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchService {

  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final CommunityRepository communityRepository;
  private final CustomMetrics customMetrics;
  private final UserSocialService userSocialService;

  public SearchService(
      PostRepository postRepository,
      CommentRepository commentRepository,
      UserRepository userRepository,
      CommunityRepository communityRepository,
      CustomMetrics customMetrics,
      UserSocialService userSocialService) {
    this.postRepository = postRepository;
    this.commentRepository = commentRepository;
    this.userRepository = userRepository;
    this.communityRepository = communityRepository;
    this.customMetrics = customMetrics;
    this.userSocialService = userSocialService;
  }

  @RateLimiter(name = "search")
  @CircuitBreaker(name = "userService")
  @Retry(name = "userService")
  @Cacheable(
      value = "searchResults",
      key =
          "#request.query() + '-' + #request.type() + '-' + #request.sort() + '-' +"
              + " #request.timeFilter() + '-' + #request.page() + '-' + #request.size() + '-' +"
              + " (#currentUserId == null ? 'anonymous' : #currentUserId)")
  public SearchResponse search(SearchRequest request, UUID currentUserId) {
    long startTime = System.currentTimeMillis();

    try {
      List<PostResponse> posts =
          request.type() == SearchRequest.SearchType.ALL
                  || request.type() == SearchRequest.SearchType.POSTS
              ? searchPosts(request)
              : List.of();
      List<CommentResponse> comments =
          request.type() == SearchRequest.SearchType.ALL
                  || request.type() == SearchRequest.SearchType.COMMENTS
              ? searchComments(request)
              : List.of();
      List<SocialUserResponse> users =
          request.type() == SearchRequest.SearchType.ALL
                  || request.type() == SearchRequest.SearchType.USERS
              ? searchUsers(request, currentUserId)
              : List.of();
      List<CommunityResponse> communities =
          request.type() == SearchRequest.SearchType.ALL
                  || request.type() == SearchRequest.SearchType.COMMUNITIES
              ? searchCommunities(request)
              : List.of();

      int totalResults = posts.size() + comments.size() + users.size() + communities.size();

      SearchResponse.SearchMetadata metadata =
          new SearchResponse.SearchMetadata(
              request.query(),
              request.type(),
              request.sort(),
              request.timeFilter(),
              totalResults,
              request.page(),
              request.size(),
              (totalResults + request.size() - 1) / request.size(),
              System.currentTimeMillis() - startTime,
              generateSuggestions(request.query()));

      return SearchResponse.ofAll(posts, comments, users, communities, metadata);

    } catch (Exception e) {
      customMetrics.recordSearchQuery("error");
      throw new RuntimeException("Search failed", e);
    }
  }

  private List<PostResponse> searchPosts(SearchRequest request) {
    Instant since = getTimeSince(request.timeFilter());
    Pageable pageable = createPageable(request);

    Page<Post> posts;
    if (!request.communities().isEmpty()) {
      // Search within specific communities
      posts =
          postRepository.searchPostsByCommunities(request.communities(), request.query(), pageable);
    } else {
      // Global search
      posts = postRepository.searchPosts(request.query(), pageable);
    }

    return posts.stream()
        .filter(post -> post.getCreatedAt().isAfter(since))
        .filter(post -> request.minScore() == null || post.getScore() >= request.minScore())
        .filter(
            post ->
                request.minComments() == null || post.getCommentCount() >= request.minComments())
        .filter(post -> request.includeNSFW() || !post.isOver18())
        .filter(post -> request.includeOver18() || !post.isOver18())
        .filter(
            post ->
                request.flairs().isEmpty()
                    || (post.getFlairText() != null
                        && request.flairs().contains(post.getFlairText())))
        .map(PostResponse::from)
        .toList();
  }

  private List<CommentResponse> searchComments(SearchRequest request) {
    Instant since = getTimeSince(request.timeFilter());
    Pageable pageable = createPageable(request);

    Page<Comment> comments = commentRepository.searchComments(request.query(), pageable);

    return comments.stream()
        .filter(comment -> comment.getCreatedAt().isAfter(since))
        .filter(comment -> request.minScore() == null || comment.getScore() >= request.minScore())
        .filter(comment -> request.includeNSFW() || !comment.isOver18())
        .filter(comment -> request.includeOver18() || !comment.isOver18())
        .map(CommentResponse::from)
        .toList();
  }

  private List<SocialUserResponse> searchUsers(SearchRequest request, UUID currentUserId) {
    Pageable pageable = createUserPageable(request);

    UserSearchTerms terms = UserSearchTerms.from(request.query());
    Page<User> users =
        userRepository.searchActiveUsers(
            terms.query(),
            terms.normalizedQuery(),
            terms.firstToken(),
            terms.lastToken(),
            pageable);

    return users.stream()
        .filter(user -> request.minScore() == null || user.getKarma() >= request.minScore())
        .map(user -> userSocialService.toSocialUserResponse(user, currentUserId))
        .toList();
  }

  private List<CommunityResponse> searchCommunities(SearchRequest request) {
    Pageable pageable = createCommunityPageable(request);

    Page<Community> communities = communityRepository.searchCommunities(request.query(), pageable);

    return communities.stream()
        .filter(community -> request.includeNSFW() || !community.isOver18())
        .filter(community -> request.includeOver18() || !community.isOver18())
        .map(CommunityResponse::from)
        .toList();
  }

  private Instant getTimeSince(SearchRequest.TimeFilter timeFilter) {
    Instant now = Instant.now();
    return switch (timeFilter) {
      case ALL -> Instant.MIN;
      case HOUR -> now.minusSeconds(3600);
      case DAY -> now.minusSeconds(86400);
      case WEEK -> now.minusSeconds(604800);
      case MONTH -> now.minusSeconds(2592000);
      case YEAR -> now.minusSeconds(31536000);
    };
  }

  private Pageable createPageable(SearchRequest request) {
    Sort sort =
        switch (request.sort()) {
          case RELEVANCE -> Sort.by(Sort.Direction.DESC, "score");
          case NEW -> Sort.by(Sort.Direction.DESC, "createdAt");
          case HOT -> Sort.by(Sort.Direction.DESC, "score");
          case TOP -> Sort.by(Sort.Direction.DESC, "score");
          case CONTROVERSIAL ->
              Sort.by(Sort.Direction.DESC, "likeCount")
                  .and(Sort.by(Sort.Direction.ASC, "dislikeCount"));
        };

    return PageRequest.of(request.page(), request.size(), sort);
  }

  private Pageable createUserPageable(SearchRequest request) {
    Sort sort =
        switch (request.sort()) {
          case RELEVANCE -> Sort.by(Sort.Direction.DESC, "karma");
          case NEW -> Sort.by(Sort.Direction.DESC, "createdAt");
          case HOT -> Sort.by(Sort.Direction.DESC, "karma");
          case TOP -> Sort.by(Sort.Direction.DESC, "karma");
          case CONTROVERSIAL -> Sort.by(Sort.Direction.DESC, "karma");
        };

    return PageRequest.of(request.page(), request.size(), sort);
  }

  private Pageable createCommunityPageable(SearchRequest request) {
    Sort sort =
        switch (request.sort()) {
          case RELEVANCE -> Sort.by(Sort.Direction.DESC, "subscriberCount");
          case NEW -> Sort.by(Sort.Direction.DESC, "createdAt");
          case HOT -> Sort.by(Sort.Direction.DESC, "subscriberCount");
          case TOP -> Sort.by(Sort.Direction.DESC, "subscriberCount");
          case CONTROVERSIAL -> Sort.by(Sort.Direction.DESC, "subscriberCount");
        };

    return PageRequest.of(request.page(), request.size(), sort);
  }

  private List<String> generateSuggestions(String query) {
    // Simple suggestion generation - in production, this could use AI or analytics data
    return List.of(
        "Try: " + query + " tutorial",
        "Try: " + query + " guide",
        "Try: " + query + " best practices",
        "Try: " + query + " examples");
  }

  private record UserSearchTerms(
      String query, String normalizedQuery, String firstToken, String lastToken) {

    private static UserSearchTerms from(String rawQuery) {
      String query = rawQuery == null ? "" : rawQuery.trim();
      String normalizedQuery = query.replaceAll("\\s+", "");
      List<String> tokens =
          Arrays.stream(query.split("\\s+")).filter(token -> !token.isBlank()).toList();
      String firstToken = tokens.isEmpty() ? query : tokens.get(0);
      String lastToken = tokens.size() < 2 ? firstToken : tokens.get(tokens.size() - 1);
      return new UserSearchTerms(query, normalizedQuery, firstToken, lastToken);
    }
  }
}
