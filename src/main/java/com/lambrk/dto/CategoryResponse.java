package com.lambrk.dto;

import com.lambrk.domain.Category;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(

    UUID id,

    String name,

    String description,

    String iconUrl,

    String imageUrl,

    String color,

    String slug,

    int sortOrder,

    int communityCount,

    Instant createdAt,

    Instant updatedAt
) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getIconUrl(),
            category.getImageUrl(),
            category.getColor(),
            category.getSlug(),
            category.getSortOrder(),
            category.getCommunities() != null ? category.getCommunities().size() : 0,
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}
