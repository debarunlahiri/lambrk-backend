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
import org.springframework.web.bind.annotation.*;

import com.lambrk.config.UserPrincipal;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

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
            @AuthenticationPrincipal UserPrincipal userDetails) {
        
        UUID authorId = getUserIdFromUserDetails(userDetails);
        PostResponse response = postService.createPost(request, authorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    @NewSpan("get-post")
    @Timed(value = "posts.get.duration", description = "Time taken to get a post")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable @SpanTag UUID postId,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        
        UUID currentUserId = getUserIdFromUserDetails(userDetails);
        PostResponse response = postService.getPost(postId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hot")
    @NewSpan("get-hot-posts")
    @Timed(value = "posts.hot.duration", description = "Time taken to get hot posts")
    public ResponseEntity<Page<PostResponse>> getHotPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score"));
        UUID currentUserId = getUserIdFromUserDetails(userDetails);
        Page<PostResponse> response = postService.getHotPosts(pageable, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/new")
    @NewSpan("get-new-posts")
    @Timed(value = "posts.new.duration", description = "Time taken to get new posts")
    public ResponseEntity<Page<PostResponse>> getNewPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        UUID currentUserId = getUserIdFromUserDetails(userDetails);
        Page<PostResponse> response = postService.getNewPosts(pageable, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top")
    @NewSpan("get-top-posts")
    @Timed(value = "posts.top.duration", description = "Time taken to get top posts")
    public ResponseEntity<Page<PostResponse>> getTopPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score"));
        UUID currentUserId = getUserIdFromUserDetails(userDetails);
        Page<PostResponse> response = postService.getTopPosts(pageable, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/community/{communityId}")
    @NewSpan("get-community-posts")
    @Timed(value = "posts.community.duration", description = "Time taken to get community posts")
    public ResponseEntity<Page<PostResponse>> getPostsByCommunity(
            @PathVariable @SpanTag UUID communityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        UUID currentUserId = getUserIdFromUserDetails(userDetails);
        Page<PostResponse> response = postService.getPostsByCommunity(communityId, pageable, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @NewSpan("get-user-posts")
    @Timed(value = "posts.user.duration", description = "Time taken to get user posts")
    public ResponseEntity<Page<PostResponse>> getPostsByUser(
            @PathVariable @SpanTag UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        UUID currentUserId = getUserIdFromUserDetails(userDetails);
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
            @AuthenticationPrincipal UserPrincipal userDetails) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score"));
        UUID currentUserId = getUserIdFromUserDetails(userDetails);
        Page<PostResponse> response = postService.searchPosts(query, pageable, currentUserId);
        return ResponseEntity.ok(response);
    }

    // =====================================
    // LoopMix Endpoints (Reels / Shorts)
    // =====================================

    @GetMapping("/media")
    @NewSpan("get-media-posts")
    @Timed(value = "posts.media.duration", description = "Time taken to get media posts")
    @Counted(value = "posts.media.fetched", extraTags = {"type", "loopmix"})
    public ResponseEntity<Page<PostResponse>> getMediaPosts(
            @RequestParam(defaultValue = "ALL") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userDetails) {

        com.lambrk.domain.Post.PostType[] mediaTypes;
        switch (type.toUpperCase()) {
            case "IMAGE" -> mediaTypes = new com.lambrk.domain.Post.PostType[]{
                com.lambrk.domain.Post.PostType.IMAGE
            };
            case "VIDEO" -> mediaTypes = new com.lambrk.domain.Post.PostType[]{
                com.lambrk.domain.Post.PostType.VIDEO
            };
            default -> mediaTypes = new com.lambrk.domain.Post.PostType[]{
                com.lambrk.domain.Post.PostType.IMAGE,
                com.lambrk.domain.Post.PostType.VIDEO
            };
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        UUID currentUserId = getUserIdFromUserDetails(userDetails);
        Page<PostResponse> response = postService.getMediaPosts(List.of(mediaTypes), pageable, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{postId}/view")
    @NewSpan("increment-post-view")
    @Timed(value = "posts.view.duration", description = "Time taken to record a view")
    @Counted(value = "posts.view.recorded", extraTags = {"type", "loopmix"})
    public ResponseEntity<Void> recordView(@PathVariable @SpanTag UUID postId) {
        postService.incrementViewCount(postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/related")
    @NewSpan("get-related-posts")
    @Timed(value = "posts.related.duration", description = "Time taken to get related posts")
    public ResponseEntity<List<PostResponse>> getRelatedPosts(
            @PathVariable @SpanTag UUID postId,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userDetails) {

        UUID currentUserId = getUserIdFromUserDetails(userDetails);
        List<PostResponse> response = postService.getRelatedPosts(postId, size, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{postId}")
    @NewSpan("update-post")
    @Timed(value = "posts.update.duration", description = "Time taken to update a post")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable @SpanTag UUID postId,
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        
        UUID currentUserId = getUserIdFromUserDetails(userDetails);
        PostResponse response = postService.updatePost(postId, request, currentUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    @NewSpan("delete-post")
    @Timed(value = "posts.delete.duration", description = "Time taken to delete a post")
    public ResponseEntity<Void> deletePost(
            @PathVariable @SpanTag UUID postId,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        
        UUID currentUserId = getUserIdFromUserDetails(userDetails);
        postService.deletePost(postId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stickied")
    @NewSpan("get-stickied-posts")
    @Timed(value = "posts.stickied.duration", description = "Time taken to get stickied posts")
    public ResponseEntity<List<PostResponse>> getStickiedPosts(
            @RequestParam(required = false) UUID communityId,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        
        UUID currentUserId = getUserIdFromUserDetails(userDetails);
        List<PostResponse> response = postService.getStickiedPosts(communityId, currentUserId);
        return ResponseEntity.ok(response);
    }

    private UUID getUserIdFromUserDetails(UserPrincipal userPrincipal) {
        return userPrincipal != null ? userPrincipal.getUserId() : null;
    }
}
