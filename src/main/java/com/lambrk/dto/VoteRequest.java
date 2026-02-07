package com.lambrk.dto;

import com.lambrk.domain.Vote;
import jakarta.validation.constraints.NotNull;

public record VoteRequest(

    @NotNull(message = "Vote type is required")
    Vote.VoteType voteType,

    Long postId,

    Long commentId
) {}
