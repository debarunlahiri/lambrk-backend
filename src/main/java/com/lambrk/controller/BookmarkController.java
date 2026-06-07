package com.lambrk.controller;

import com.lambrk.config.UserPrincipal;
import com.lambrk.domain.Post;
import com.lambrk.dto.PostResponse;
import com.lambrk.service.BookmarkService;
import com.lambrk.service.PostService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final PostService postService;

    public BookmarkController(BookmarkService bookmarkService, PostService postService) {
        this.bookmarkService = bookmarkService;
        this.postService = postService;
    }

    @PostMapping("/{postId}")
    @NewSpan("bookmark-post")
    @Counted(value = "bookmarks.created", extraTags = {"action", "bookmark"})
    @Timed(value = "bookmarks.create.duration", description = "Time taken to bookmark a post")
    public ResponseEntity<Void> bookmarkPost(
            @PathVariable @SpanTag UUID postId,
            @AuthenticationPrincipal UserPrincipal userDetails) {

        UUID userId = userDetails.getUserId();
        bookmarkService.bookmarkPost(userId, postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}")
    @NewSpan("unbookmark-post")
    @Counted(value = "bookmarks.deleted", extraTags = {"action", "unbookmark"})
    @Timed(value = "bookmarks.delete.duration", description = "Time taken to unbookmark a post")
    public ResponseEntity<Void> unbookmarkPost(
            @PathVariable @SpanTag UUID postId,
            @AuthenticationPrincipal UserPrincipal userDetails) {

        UUID userId = userDetails.getUserId();
        bookmarkService.unbookmarkPost(userId, postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @NewSpan("get-bookmarks")
    @Timed(value = "bookmarks.list.duration", description = "Time taken to get user's bookmarks")
    public ResponseEntity<Page<PostResponse>> getBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userDetails) {

        UUID userId = userDetails.getUserId();
        Page<Post> posts = bookmarkService.getBookmarkedPosts(userId, page, size);
        Page<PostResponse> response = posts.map(post ->
            PostResponse.from(post, postService.getUserVote(post, userId), bookmarkService.isBookmarked(userId, post.getId()))
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/status")
    @NewSpan("check-bookmark-status")
    public ResponseEntity<Map<String, Boolean>> isBookmarked(
            @PathVariable @SpanTag UUID postId,
            @AuthenticationPrincipal UserPrincipal userDetails) {

        UUID userId = userDetails.getUserId();
        boolean bookmarked = bookmarkService.isBookmarked(userId, postId);
        return ResponseEntity.ok(Map.of("bookmarked", bookmarked));
    }

    @GetMapping("/count")
    @NewSpan("get-bookmark-count")
    public ResponseEntity<Map<String, Long>> getBookmarkCount(
            @AuthenticationPrincipal UserPrincipal userDetails) {

        UUID userId = userDetails.getUserId();
        long count = bookmarkService.getBookmarkCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
