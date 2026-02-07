package com.lambrk.controller;

import com.lambrk.dto.VoteRequest;
import com.lambrk.service.VoteService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping("/post")
    @NewSpan("vote-on-post")
    @Counted(value = "votes.post.cast")
    @Timed(value = "votes.post.duration")
    public ResponseEntity<Void> voteOnPost(
            @Valid @RequestBody VoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        voteService.voteOnPost(request, getUserId(userDetails));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comment")
    @NewSpan("vote-on-comment")
    @Counted(value = "votes.comment.cast")
    @Timed(value = "votes.comment.duration")
    public ResponseEntity<Void> voteOnComment(
            @Valid @RequestBody VoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        voteService.voteOnComment(request, getUserId(userDetails));
        return ResponseEntity.ok().build();
    }

    private Long getUserId(UserDetails userDetails) {
        return userDetails != null ? 1L : null;
    }
}
