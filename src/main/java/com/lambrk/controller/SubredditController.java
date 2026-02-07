package com.lambrk.controller;

import com.lambrk.dto.SubredditCreateRequest;
import com.lambrk.dto.SubredditResponse;
import com.lambrk.service.SubredditService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/subreddits")
public class SubredditController {

    private final SubredditService subredditService;

    public SubredditController(SubredditService subredditService) {
        this.subredditService = subredditService;
    }

    @PostMapping
    @NewSpan("create-subreddit")
    @Counted(value = "subreddits.created")
    @Timed(value = "subreddits.create.duration")
    public ResponseEntity<SubredditResponse> createSubreddit(
            @Valid @RequestBody SubredditCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(subredditService.createSubreddit(request, getUserId(userDetails)));
    }

    @GetMapping("/{subredditId}")
    @NewSpan("get-subreddit")
    @Timed(value = "subreddits.get.duration")
    public ResponseEntity<SubredditResponse> getSubreddit(
            @PathVariable @SpanTag Long subredditId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(subredditService.getSubreddit(subredditId, getUserId(userDetails)));
    }

    @GetMapping("/r/{name}")
    @NewSpan("get-subreddit-by-name")
    @Timed(value = "subreddits.getByName.duration")
    public ResponseEntity<SubredditResponse> getSubredditByName(
            @PathVariable @SpanTag String name,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(subredditService.getSubredditByName(name, getUserId(userDetails)));
    }

    @GetMapping("/trending")
    @NewSpan("get-trending-subreddits")
    @Timed(value = "subreddits.trending.duration")
    public ResponseEntity<Page<SubredditResponse>> getTrendingSubreddits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(subredditService.getTrendingSubreddits(pageable));
    }

    @GetMapping
    @NewSpan("get-public-subreddits")
    @Timed(value = "subreddits.public.duration")
    public ResponseEntity<Page<SubredditResponse>> getPublicSubreddits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "subscriberCount"));
        return ResponseEntity.ok(subredditService.getPublicSubreddits(pageable));
    }

    @GetMapping("/search")
    @NewSpan("search-subreddits")
    @Timed(value = "subreddits.search.duration")
    public ResponseEntity<Page<SubredditResponse>> searchSubreddits(
            @RequestParam @SpanTag String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(subredditService.searchSubreddits(query, pageable));
    }

    @PutMapping("/{subredditId}")
    @NewSpan("update-subreddit")
    @Timed(value = "subreddits.update.duration")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<SubredditResponse> updateSubreddit(
            @PathVariable @SpanTag Long subredditId,
            @Valid @RequestBody SubredditCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(subredditService.updateSubreddit(subredditId, request, getUserId(userDetails)));
    }

    @PostMapping("/{subredditId}/subscribe")
    @NewSpan("subscribe-subreddit")
    @Timed(value = "subreddits.subscribe.duration")
    public ResponseEntity<Void> subscribe(
            @PathVariable @SpanTag Long subredditId,
            @AuthenticationPrincipal UserDetails userDetails) {
        subredditService.subscribe(subredditId, getUserId(userDetails));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{subredditId}/unsubscribe")
    @NewSpan("unsubscribe-subreddit")
    @Timed(value = "subreddits.unsubscribe.duration")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable @SpanTag Long subredditId,
            @AuthenticationPrincipal UserDetails userDetails) {
        subredditService.unsubscribe(subredditId, getUserId(userDetails));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/subscriptions")
    @NewSpan("get-user-subscriptions")
    @Timed(value = "subreddits.subscriptions.duration")
    public ResponseEntity<Set<SubredditResponse>> getUserSubscriptions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(subredditService.getUserSubscriptions(getUserId(userDetails)));
    }

    private Long getUserId(UserDetails userDetails) {
        return userDetails != null ? 1L : null;
    }
}
