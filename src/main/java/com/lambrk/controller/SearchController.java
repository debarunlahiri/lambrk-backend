package com.lambrk.controller;

import com.lambrk.dto.SearchRequest;
import com.lambrk.dto.SearchResponse;
import com.lambrk.service.SearchService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    @NewSpan("advanced-search")
    @Counted(value = "search.advanced")
    @Timed(value = "search.advanced.duration")
    public ResponseEntity<SearchResponse> advancedSearch(
            @Valid @RequestBody SearchRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts")
    @NewSpan("search-posts")
    @Counted(value = "search.posts")
    @Timed(value = "search.posts.duration")
    public ResponseEntity<SearchResponse> searchPosts(
            @RequestParam @SpanTag String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "RELEVANCE") SearchRequest.SortBy sort,
            @RequestParam(defaultValue = "ALL") SearchRequest.TimeFilter timeFilter,
            @RequestParam(required = false) List<String> subreddits,
            @RequestParam(required = false) List<String> flairs,
            @RequestParam(defaultValue = "false") boolean includeNSFW,
            @RequestParam(defaultValue = "false") boolean includeOver18,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer minComments,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        SearchRequest request = new SearchRequest(
            query,
            SearchRequest.SearchType.POSTS,
            sort,
            timeFilter,
            subreddits,
            flairs,
            includeNSFW,
            includeOver18,
            minScore,
            minComments,
            null,
            page,
            size
        );
        
        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/comments")
    @NewSpan("search-comments")
    @Counted(value = "search.comments")
    @Timed(value = "search.comments.duration")
    public ResponseEntity<SearchResponse> searchComments(
            @RequestParam @SpanTag String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "RELEVANCE") SearchRequest.SortBy sort,
            @RequestParam(defaultValue = "ALL") SearchRequest.TimeFilter timeFilter,
            @RequestParam(defaultValue = "false") boolean includeNSFW,
            @RequestParam(defaultValue = "false") boolean includeOver18,
            @RequestParam(required = false) Integer minScore,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        SearchRequest request = new SearchRequest(
            query,
            SearchRequest.SearchType.COMMENTS,
            sort,
            timeFilter,
            List.of(),
            List.of(),
            includeNSFW,
            includeOver18,
            minScore,
            null,
            null,
            page,
            size
        );
        
        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    @NewSpan("search-users")
    @Counted(value = "search.users")
    @Timed(value = "search.users.duration")
    public ResponseEntity<SearchResponse> searchUsers(
            @RequestParam @SpanTag String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "RELEVANCE") SearchRequest.SortBy sort,
            @RequestParam(required = false) Integer minScore,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        SearchRequest request = new SearchRequest(
            query,
            SearchRequest.SearchType.USERS,
            sort,
            SearchRequest.TimeFilter.ALL,
            List.of(),
            List.of(),
            false,
            false,
            minScore,
            null,
            null,
            page,
            size
        );
        
        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subreddits")
    @NewSpan("search-subreddits")
    @Counted(value = "search.subreddits")
    @Timed(value = "search.subreddits.duration")
    public ResponseEntity<SearchResponse> searchSubreddits(
            @RequestParam @SpanTag String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "RELEVANCE") SearchRequest.SortBy sort,
            @RequestParam(defaultValue = "false") boolean includeNSFW,
            @RequestParam(defaultValue = "false") boolean includeOver18,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        SearchRequest request = new SearchRequest(
            query,
            SearchRequest.SearchType.SUBREDDITS,
            sort,
            SearchRequest.TimeFilter.ALL,
            List.of(),
            List.of(),
            includeNSFW,
            includeOver18,
            null,
            null,
            null,
            page,
            size
        );
        
        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @NewSpan("search-all")
    @Counted(value = "search.all")
    @Timed(value = "search.all.duration")
    public ResponseEntity<SearchResponse> searchAll(
            @RequestParam @SpanTag String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "RELEVANCE") SearchRequest.SortBy sort,
            @RequestParam(defaultValue = "ALL") SearchRequest.TimeFilter timeFilter,
            @RequestParam(required = false) List<String> subreddits,
            @RequestParam(required = false) List<String> flairs,
            @RequestParam(defaultValue = "false") boolean includeNSFW,
            @RequestParam(defaultValue = "false") boolean includeOver18,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer minComments,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        SearchRequest request = new SearchRequest(
            query,
            SearchRequest.SearchType.ALL,
            sort,
            timeFilter,
            subreddits,
            flairs,
            includeNSFW,
            includeOver18,
            minScore,
            minComments,
            null,
            page,
            size
        );
        
        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggestions")
    @NewSpan("search-suggestions")
    @Counted(value = "search.suggestions")
    @Timed(value = "search.suggestions.duration")
    public ResponseEntity<List<String>> getSearchSuggestions(
            @RequestParam @SpanTag String query,
            @RequestParam(defaultValue = "posts") String type) {
        
        // This would use the SearchRepository suggestion methods
        List<String> suggestions = switch (type) {
            case "posts" -> List.of(
                query + " tutorial",
                query + " guide",
                query + " examples",
                query + " best practices"
            );
            case "subreddits" -> List.of(
                "r/" + query.toLowerCase(),
                "r/" + query.toLowerCase() + "discussion",
                "r/" + query.toLowerCase() + "help"
            );
            case "users" -> List.of(
                query + "user",
                query + "dev",
                query + "official"
            );
            default -> List.of();
        };
        
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/trending")
    @NewSpan("search-trending")
    @Counted(value = "search.trending")
    @Timed(value = "search.trending.duration")
    public ResponseEntity<SearchResponse> getTrendingSearches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // This would return trending search terms
        SearchResponse response = new SearchResponse(
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            new SearchResponse.SearchMetadata(
                "trending",
                SearchRequest.SearchType.ALL,
                SearchRequest.SortBy.HOT,
                SearchRequest.TimeFilter.WEEK,
                0,
                page,
                size,
                0,
                0,
                List.of("spring boot", "java 25", "virtual threads", "kubernetes")
            )
        );
        
        return ResponseEntity.ok(response);
    }
}
