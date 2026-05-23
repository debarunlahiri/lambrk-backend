package com.lambrk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    String name,

    String description,

    String iconUrl,

    String imageUrl,

    String color,

    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9\\-]+$", message = "Slug can only contain lowercase letters, numbers, and hyphens")
    @Size(max = 50, message = "Slug must be less than 50 characters")
    String slug,

    int sortOrder
) {}
