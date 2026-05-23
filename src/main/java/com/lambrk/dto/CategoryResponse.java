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
            category.id(),
            category.name(),
            category.description(),
            category.iconUrl(),
            category.imageUrl(),
            category.color(),
            category.slug(),
            category.sortOrder(),
            category.communities() != null ? category.communities().size() : 0,
            category.createdAt(),
            category.updatedAt()
        );
    }
}
