package com.lambrk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(

    @NotBlank(message = "Content is required")
    @Size(max = 10000, message = "Comment must be less than 10000 characters")
    String content,

    Long postId,

    Long parentCommentId
) {}
