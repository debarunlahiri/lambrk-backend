package com.lambrk.dto;

import com.lambrk.domain.User;

import java.time.Instant;
import java.util.UUID;

public record SocialUserResponse(
    UUID id,
    String username,
    String displayName,
    String bio,
    String avatarUrl,
    String headerImageUrl,
    String location,
    String website,
    boolean isVerified,
    int karma,
    long followerCount,
    long followingCount,
    long friendCount,
    boolean followedByCurrentUser,
    boolean followingCurrentUser,
    boolean friend,
    String friendshipStatus,
    Instant createdAt
) {

    public static SocialUserResponse from(
        User user,
        SocialStatsResponse stats,
        boolean followedByCurrentUser,
        boolean followingCurrentUser,
        boolean friend,
        String friendshipStatus
    ) {
        return new SocialUserResponse(
            user.getId(),
            user.getUsername(),
            user.getDisplayName(),
            user.getBio(),
            com.lambrk.util.CdnUrlResolver.resolve(user.getAvatarUrl()),
            com.lambrk.util.CdnUrlResolver.resolve(user.getHeaderImageUrl()),
            user.getLocation(),
            user.getWebsite(),
            user.isVerified(),
            user.getKarma(),
            stats.followerCount(),
            stats.followingCount(),
            stats.friendCount(),
            followedByCurrentUser,
            followingCurrentUser,
            friend,
            friendshipStatus,
            user.getCreatedAt()
        );
    }
}
