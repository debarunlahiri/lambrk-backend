package com.lambrk.dto;

import com.lambrk.domain.Community;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommunityResponse(

    UUID id,

    String name,

    String title,

    String description,

    String sidebarText,

    String headerImageUrl,

    String iconImageUrl,

    boolean isPublic,

    boolean isRestricted,

    boolean isOver18,

    int memberCount,

    int subscriberCount,

    int activeUserCount,

    UserResponse createdBy,

    List<CategoryResponse> categories,

    Instant createdAt,

    Instant updatedAt,

    boolean isUserSubscribed,

    boolean isUserModerator
) {
    
    public static CommunityResponse from(Community community) {
        return new CommunityResponse(
            community.getId(),
            community.getName(),
            community.getTitle(),
            community.getDescription(),
            community.getSidebarText(),
            com.lambrk.util.CdnUrlResolver.resolve(community.getHeaderImageUrl()),
            com.lambrk.util.CdnUrlResolver.resolve(community.getIconImageUrl()),
            community.isPublic(),
            community.isRestricted(),
            community.isOver18(),
            community.getMemberCount(),
            community.getSubscriberCount(),
            community.getActiveUserCount(),
            UserResponse.from(community.getCreatedBy()),
            community.getCategories() != null
                ? community.getCategories().stream().map(CategoryResponse::from).toList()
                : List.of(),
            community.getCreatedAt(),
            community.getUpdatedAt(),
            false,
            false
        );
    }

    public static CommunityResponse from(Community community, boolean isUserSubscribed, boolean isUserModerator) {
        return new CommunityResponse(
            community.getId(),
            community.getName(),
            community.getTitle(),
            community.getDescription(),
            community.getSidebarText(),
            com.lambrk.util.CdnUrlResolver.resolve(community.getHeaderImageUrl()),
            com.lambrk.util.CdnUrlResolver.resolve(community.getIconImageUrl()),
            community.isPublic(),
            community.isRestricted(),
            community.isOver18(),
            community.getMemberCount(),
            community.getSubscriberCount(),
            community.getActiveUserCount(),
            UserResponse.from(community.getCreatedBy()),
            community.getCategories() != null
                ? community.getCategories().stream().map(CategoryResponse::from).toList()
                : List.of(),
            community.getCreatedAt(),
            community.getUpdatedAt(),
            isUserSubscribed,
            isUserModerator
        );
    }
}
