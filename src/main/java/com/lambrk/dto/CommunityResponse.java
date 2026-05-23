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
            community.id(),
            community.name(),
            community.title(),
            community.description(),
            community.sidebarText(),
            community.headerImageUrl(),
            community.iconImageUrl(),
            community.isPublic(),
            community.isRestricted(),
            community.isOver18(),
            community.memberCount(),
            community.subscriberCount(),
            community.activeUserCount(),
            UserResponse.from(community.createdBy()),
            community.categories() != null
                ? community.categories().stream().map(CategoryResponse::from).toList()
                : List.of(),
            community.createdAt(),
            community.updatedAt(),
            false,
            false
        );
    }

    public static CommunityResponse from(Community community, boolean isUserSubscribed, boolean isUserModerator) {
        return new CommunityResponse(
            community.id(),
            community.name(),
            community.title(),
            community.description(),
            community.sidebarText(),
            community.headerImageUrl(),
            community.iconImageUrl(),
            community.isPublic(),
            community.isRestricted(),
            community.isOver18(),
            community.memberCount(),
            community.subscriberCount(),
            community.activeUserCount(),
            UserResponse.from(community.createdBy()),
            community.categories() != null
                ? community.categories().stream().map(CategoryResponse::from).toList()
                : List.of(),
            community.createdAt(),
            community.updatedAt(),
            isUserSubscribed,
            isUserModerator
        );
    }
}
