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
    boolean privateAccount,
    boolean canViewFollowerCount,
    boolean canViewFollowingCount,
    boolean canViewFollowerList,
    boolean canViewFollowingList,
    boolean canShowAddFriendButton,
    boolean canShowFollowButton,
    boolean canShowInMutualLists,
    boolean messageButtonEnabled,
    Instant createdAt) {

  public static SocialUserResponse from(
      User user,
      SocialStatsResponse stats,
      boolean followedByCurrentUser,
      boolean followingCurrentUser,
      boolean friend,
      String friendshipStatus,
      boolean canViewFollowerCount,
      boolean canViewFollowingCount,
      boolean canViewFollowerList,
      boolean canViewFollowingList,
      boolean canShowAddFriendButton,
      boolean canShowFollowButton,
      boolean canShowInMutualLists) {
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
        canViewFollowerCount ? stats.followerCount() : 0,
        canViewFollowingCount ? stats.followingCount() : 0,
        stats.friendCount(),
        followedByCurrentUser,
        followingCurrentUser,
        friend,
        friendshipStatus,
        user.isPrivateAccount(),
        canViewFollowerCount,
        canViewFollowingCount,
        canViewFollowerList,
        canViewFollowingList,
        canShowAddFriendButton,
        canShowFollowButton,
        canShowInMutualLists,
        user.isMessageButtonEnabled(),
        user.getCreatedAt());
  }
}
