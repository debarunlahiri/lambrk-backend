package com.lambrk.service;

import com.lambrk.dto.SearchRequest;
import com.lambrk.dto.SearchResponse;
import com.lambrk.dto.PostResponse;
import com.lambrk.dto.CommentResponse;
import com.lambrk.dto.UserResponse;
import com.lambrk.dto.SubredditResponse;
import com.lambrk.domain.Post;
import com.lambrk.domain.Comment;
import com.lambrk.domain.User;
import com.lambrk.domain.Subreddit;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.CommentRepository;
import com.lambrk.repository.UserRepository;
import com.lambrk.repository.SubredditRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

@Service
@Transactional(readOnly = true)
public class SearchService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final SubredditRepository subredditRepository;
    private final CustomMetrics customMetrics;

    public SearchService(PostRepository postRepository, CommentRepository commentRepository,
                         UserRepository userRepository, SubredditRepository subredditRepository,
                         CustomMetrics customMetrics) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.subredditRepository = subredditRepository;
        this.customMetrics = customMetrics;
    }

    @RateLimiter(name = "search")
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    @Cacheable(value = "searchResults", key = "#request.query() + '-' + #request.type() + '-' + #request.page()")
    public SearchResponse search(SearchRequest request) {
        long startTime = System.currentTimeMillis();
        
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            
            var postsFuture = scope.fork(() -> 
                request.type() == SearchRequest.SearchType.ALL || request.type() == SearchRequest.SearchType.POSTS
                    ? searchPosts(request)
                    : List.of());
            
            var commentsFuture = scope.fork(() -> 
                request.type() == SearchRequest.SearchType.ALL || request.type() == SearchRequest.SearchType.COMMENTS
                    ? searchComments(request)
                    : List.of());
            
            var usersFuture = scope.fork(() -> 
                request.type() == SearchRequest.SearchType.ALL || request.type() == SearchRequest.SearchType.USERS
                    ? searchUsers(request)
                    : List.of());
            
            var subredditsFuture = scope.fork(() -> 
                request.type() == SearchRequest.SearchType.ALL || request.type() == SearchRequest.SearchType.SUBREDDITS
                    ? searchSubreddits(request)
                    : List.of());
            
            scope.join();
            scope.throwIfFailed();
            
            List<PostResponse> posts = postsFuture.get();
            List<CommentResponse> comments = commentsFuture.get();
            List<UserResponse> users = usersFuture.get();
            List<SubredditResponse> subreddits = subredditsFuture.get();
            
            int totalResults = posts.size() + comments.size() + users.size() + subreddits.size();
            
            SearchResponse.SearchMetadata metadata = new SearchResponse.SearchMetadata(
                request.query(),
                request.type(),
                request.sort(),
                request.timeFilter(),
                totalResults,
                request.page(),
                request.size(),
                (totalResults + request.size() - 1) / request.size(),
                System.currentTimeMillis() - startTime,
                generateSuggestions(request.query())
            );
            
            return SearchResponse.ofAll(posts, comments, users, subreddits, metadata);
            
        } catch (Exception e) {
            customMetrics.recordSearchQuery("error");
            throw new RuntimeException("Search failed", e);
        }
    }

    private List<PostResponse> searchPosts(SearchRequest request) {
        Instant since = getTimeSince(request.timeFilter());
        Pageable pageable = createPageable(request);
        
        Page<Post> posts;
        if (!request.subreddits().isEmpty()) {
            // Search within specific subreddits
            posts = postRepository.searchPostsBySubreddits(request.subreddits(), request.query(), pageable);
        } else {
            // Global search
            posts = postRepository.searchPosts(request.query(), pageable);
        }
        
        return posts.stream()
            .filter(post -> post.createdAt().isAfter(since))
            .filter(post -> request.minScore() == null || post.score() >= request.minScore())
            .filter(post -> request.minComments() == null || post.commentCount() >= request.minComments())
            .filter(post -> request.includeNSFW() || !post.isOver18())
            .filter(post -> request.includeOver18() || !post.isOver18())
            .filter(post -> request.flairs().isEmpty() || 
                (post.flairText() != null && request.flairs().contains(post.flairText())))
            .map(PostResponse::from)
            .toList();
    }

    private List<CommentResponse> searchComments(SearchRequest request) {
        Instant since = getTimeSince(request.timeFilter());
        Pageable pageable = createPageable(request);
        
        Page<Comment> comments = commentRepository.searchComments(request.query(), pageable);
        
        return comments.stream()
            .filter(comment -> comment.createdAt().isAfter(since))
            .filter(comment -> request.minScore() == null || comment.score() >= request.minScore())
            .filter(comment -> request.includeNSFW() || !comment.isOver18())
            .filter(comment -> request.includeOver18() || !comment.isOver18())
            .map(CommentResponse::from)
            .toList();
    }

    private List<UserResponse> searchUsers(SearchRequest request) {
        Pageable pageable = createPageable(request);
        
        Page<User> users = userRepository.searchActiveUsers(request.query(), pageable);
        
        return users.stream()
            .filter(user -> request.minScore() == null || user.karma() >= request.minScore())
            .map(UserResponse::from)
            .toList();
    }

    private List<SubredditResponse> searchSubreddits(SearchRequest request) {
        Pageable pageable = createPageable(request);
        
        Page<Subreddit> subreddits = subredditRepository.searchSubreddits(request.query(), pageable);
        
        return subreddits.stream()
            .filter(subreddit -> request.includeNSFW() || !subreddit.isOver18())
            .filter(subreddit -> request.includeOver18() || !subreddit.isOver18())
            .map(SubredditResponse::from)
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
        Sort sort = switch (request.sort()) {
            case RELEVANCE -> Sort.by(Sort.Direction.DESC, "score");
            case NEW -> Sort.by(Sort.Direction.DESC, "createdAt");
            case HOT -> Sort.by(Sort.Direction.DESC, "score");
            case TOP -> Sort.by(Sort.Direction.DESC, "score");
            case CONTROVERSIAL -> Sort.by(Sort.Direction.DESC, "upvoteCount").and(Sort.by(Sort.Direction.ASC, "downvoteCount"));
        };
        
        return PageRequest.of(request.page(), request.size(), sort);
    }

    private List<String> generateSuggestions(String query) {
        // Simple suggestion generation - in production, this could use AI or analytics data
        return List.of(
            "Try: " + query + " tutorial",
            "Try: " + query + " guide",
            "Try: " + query + " best practices",
            "Try: " + query + " examples"
        );
    }
}
