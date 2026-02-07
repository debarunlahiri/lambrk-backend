package com.lambrk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubredditCreateRequest(

    @NotBlank(message = "Subreddit name is required")
    @Size(min = 3, max = 21, message = "Name must be between 3 and 21 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Name can only contain letters, numbers, and underscores")
    String name,

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    String title,

    String description,

    String sidebarText,

    boolean isPublic,

    boolean isRestricted,

    boolean isOver18
) {}
