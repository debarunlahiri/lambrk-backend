package com.lambrk.dto;

import com.lambrk.domain.Vote;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VoteRequest(
    @NotNull(message = "Vote type is required") Vote.VoteType voteType,
    UUID postId,
    UUID commentId) {}
