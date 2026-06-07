package com.lambrk.dto;

import com.lambrk.domain.User;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    String displayName,
    String bio,
    String avatarUrl,
    String headerImageUrl,
    String location,
    String website,
    boolean isActive,
    boolean isVerified,
    int karma,
    boolean privateAccount,
    boolean hideFollowerCount,
    boolean hideFollowingCount,
    boolean hideFollowerList,
    boolean hideFollowingList,
    boolean hideAddFriendButton,
    boolean hideFollowButton,
    boolean hideFromMutualList,
    boolean messageButtonEnabled,
    Instant createdAt,
    Instant updatedAt) {

  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getDisplayName(),
        user.getBio(),
        com.lambrk.util.CdnUrlResolver.resolve(user.getAvatarUrl()),
        com.lambrk.util.CdnUrlResolver.resolve(user.getHeaderImageUrl()),
        user.getLocation(),
        user.getWebsite(),
        user.isActive(),
        user.isVerified(),
        user.getKarma(),
        user.isPrivateAccount(),
        user.isHideFollowerCount(),
        user.isHideFollowingCount(),
        user.isHideFollowerList(),
        user.isHideFollowingList(),
        user.isHideAddFriendButton(),
        user.isHideFollowButton(),
        user.isHideFromMutualList(),
        user.isMessageButtonEnabled(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}
