package com.lambrk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CommentCreateRequest(

    @NotBlank(message = "Content is required")
    @Size(max = 10000, message = "Comment must be less than 10000 characters")
    String content,

    UUID postId,

    UUID parentCommentId
) {}
