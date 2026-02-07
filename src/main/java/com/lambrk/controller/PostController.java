package com.lambrk.controller;

import com.lambrk.dto.PostCreateRequest;
import com.lambrk.dto.PostResponse;
import com.lambrk.service.PostService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @NewSpan("create-post")
    @Counted(value = "posts.created", extraTags = {"type", "post"})
    @Timed(value = "posts.create.duration", description = "Time taken to create a post")
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long authorId = getUserIdFromUserDetails(userDetails);
        PostResponse response = postService.createPost(request, authorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    @NewSpan("get-post")
    @Timed(value = "posts.get.duration", description = "Time taken to get a post")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable @SpanTag Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long currentUserId = getUserIdFromUserDetails(userDetails);
        PostResponse response = postService.getPost(postId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hot")
    @NewSpan("get-hot-posts")
    @Timed(value = "posts.hot.duration", description = "Time taken to get hot posts")
    public ResponseEntity<Page<PostResponse>> getHotPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score"));
        Long currentUserId = getUserIdFromUserDetails(userDetails);
        Page<PostResponse> response = postService.getHotPosts(pageable, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/new")
    @NewSpan("get-new-posts")
    @Timed(value = "posts.new.duration", description = "Time taken to get new posts")
    public ResponseEntity<Page<PostResponse>> getNewPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Long currentUserId = getUserIdFromUserDetails(userDetails);
        Page<PostResponse> response = postService.getNewPosts(pageable, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top")
    @NewSpan("get-top-posts")
    @Timed(value = "posts.top.duration", description = "Time taken to get top posts")
    public ResponseEntity<Page<PostResponse>> getTopPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score"));
        Long currentUserId = getUserIdFromUserDetails(userDetails);
        Page<PostResponse> response = postService.getTopPosts(pageable, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subreddit/{subredditId}")
    @NewSpan("get-subreddit-posts")
    @Timed(value = "posts.subreddit.duration", description = "Time taken to get subreddit posts")
    public ResponseEntity<Page<PostResponse>> getPostsBySubreddit(
            @PathVariable @SpanTag Long subredditId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Long currentUserId = getUserIdFromUserDetails(userDetails);
        Page<PostResponse> response = postService.getPostsBySubreddit(subredditId, pageable, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @NewSpan("get-user-posts")
    @Timed(value = "posts.user.duration", description = "Time taken to get user posts")
    public ResponseEntity<Page<PostResponse>> getPostsByUser(
            @PathVariable @SpanTag Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Long currentUserId = getUserIdFromUserDetails(userDetails);
        Page<PostResponse> response = postService.getPostsByUser(userId, pageable, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @NewSpan("search-posts")
    @Timed(value = "posts.search.duration", description = "Time taken to search posts")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @RequestParam @SpanTag String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score"));
        Long currentUserId = getUserIdFromUserDetails(userDetails);
        Page<PostResponse> response = postService.searchPosts(query, pageable, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{postId}")
    @NewSpan("update-post")
    @Timed(value = "posts.update.duration", description = "Time taken to update a post")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable @SpanTag Long postId,
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long currentUserId = getUserIdFromUserDetails(userDetails);
        PostResponse response = postService.updatePost(postId, request, currentUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    @NewSpan("delete-post")
    @Timed(value = "posts.delete.duration", description = "Time taken to delete a post")
    public ResponseEntity<Void> deletePost(
            @PathVariable @SpanTag Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long currentUserId = getUserIdFromUserDetails(userDetails);
        postService.deletePost(postId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stickied")
    @NewSpan("get-stickied-posts")
    @Timed(value = "posts.stickied.duration", description = "Time taken to get stickied posts")
    public ResponseEntity<List<PostResponse>> getStickiedPosts(
            @RequestParam(required = false) Long subredditId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long currentUserId = getUserIdFromUserDetails(userDetails);
        List<PostResponse> response = postService.getStickiedPosts(subredditId, currentUserId);
        return ResponseEntity.ok(response);
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        // In a real implementation, you would extract the user ID from the JWT token or user details
        // For now, we'll use a placeholder implementation
        return 1L; // This should be replaced with actual user ID extraction
    }
}
