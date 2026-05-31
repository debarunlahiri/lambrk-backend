package com.lambrk.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

    @Size(max = 100, message = "Display name must be less than 100 characters")
    String displayName,

    @Size(max = 500, message = "Bio must be less than 500 characters")
    String bio,

    @Size(max = 100, message = "Location must be less than 100 characters")
    String location,

    @Size(max = 200, message = "Website must be less than 200 characters")
    String website,

    @Size(max = 500, message = "Avatar URL must be less than 500 characters")
    String avatarUrl,

    @Size(max = 500, message = "Header image URL must be less than 500 characters")
    String headerImageUrl
) {}
