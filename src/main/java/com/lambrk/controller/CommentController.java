package com.lambrk.controller;

import com.lambrk.dto.CommentCreateRequest;
import com.lambrk.dto.CommentResponse;
import com.lambrk.service.CommentService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @NewSpan("create-comment")
    @Counted(value = "comments.created")
    @Timed(value = "comments.create.duration")
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long authorId = getUserId(userDetails);
        return ResponseEntity.ok(commentService.createComment(request, authorId));
    }

    @GetMapping("/{commentId}")
    @NewSpan("get-comment")
    @Timed(value = "comments.get.duration")
    public ResponseEntity<CommentResponse> getComment(
            @PathVariable @SpanTag Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(commentService.getComment(commentId, getUserId(userDetails)));
    }

    @GetMapping("/post/{postId}")
    @NewSpan("get-post-comments")
    @Timed(value = "comments.post.duration")
    public ResponseEntity<Page<CommentResponse>> getCommentsByPost(
            @PathVariable @SpanTag Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score"));
        return ResponseEntity.ok(commentService.getCommentsByPost(postId, pageable, getUserId(userDetails)));
    }

    @GetMapping("/{commentId}/replies")
    @NewSpan("get-comment-replies")
    @Timed(value = "comments.replies.duration")
    public ResponseEntity<List<CommentResponse>> getReplies(
            @PathVariable @SpanTag Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(commentService.getReplies(commentId, getUserId(userDetails)));
    }

    @GetMapping("/user/{userId}")
    @NewSpan("get-user-comments")
    @Timed(value = "comments.user.duration")
    public ResponseEntity<Page<CommentResponse>> getCommentsByUser(
            @PathVariable @SpanTag Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(commentService.getCommentsByUser(userId, pageable, getUserId(userDetails)));
    }

    @PutMapping("/{commentId}")
    @NewSpan("update-comment")
    @Timed(value = "comments.update.duration")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable @SpanTag Long commentId,
            @RequestBody String newContent,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(commentService.updateComment(commentId, newContent, getUserId(userDetails)));
    }

    @DeleteMapping("/{commentId}")
    @NewSpan("delete-comment")
    @Timed(value = "comments.delete.duration")
    public ResponseEntity<Void> deleteComment(
            @PathVariable @SpanTag Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        commentService.deleteComment(commentId, getUserId(userDetails));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @NewSpan("search-comments")
    @Timed(value = "comments.search.duration")
    public ResponseEntity<Page<CommentResponse>> searchComments(
            @RequestParam @SpanTag String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score"));
        return ResponseEntity.ok(commentService.searchComments(query, pageable, getUserId(userDetails)));
    }

    private Long getUserId(UserDetails userDetails) {
        return userDetails != null ? 1L : null; // Replace with actual user ID extraction
    }
}
