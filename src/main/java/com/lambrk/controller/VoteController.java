package com.lambrk.controller;

import com.lambrk.dto.VoteRequest;
import com.lambrk.service.VoteService;
import java.util.UUID;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.lambrk.config.UserPrincipal;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping("/post")
    @NewSpan("like-on-post")
    @Counted(value = "likes.post.cast")
    @Timed(value = "likes.post.duration")
    public ResponseEntity<Void> voteOnPost(
            @Valid @RequestBody VoteRequest request,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        voteService.voteOnPost(request, getUserId(userDetails));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comment")
    @NewSpan("like-on-comment")
    @Counted(value = "likes.comment.cast")
    @Timed(value = "likes.comment.duration")
    public ResponseEntity<Void> voteOnComment(
            @Valid @RequestBody VoteRequest request,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        voteService.voteOnComment(request, getUserId(userDetails));
        return ResponseEntity.ok().build();
    }

    private UUID getUserId(UserPrincipal userPrincipal) {
        return userPrincipal != null ? userPrincipal.getUserId() : null;
    }
}
