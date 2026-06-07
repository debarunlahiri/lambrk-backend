package com.lambrk.dto;

import com.lambrk.domain.User;
import java.time.Instant;
import java.util.UUID;

public record UserPrivacySettingsResponse(
    UUID userId,
    boolean privateAccount,
    boolean hideFollowerCount,
    boolean hideFollowingCount,
    boolean hideFollowerList,
    boolean hideFollowingList,
    boolean hideAddFriendButton,
    boolean hideFollowButton,
    boolean hideFromMutualList,
    boolean messageButtonEnabled,
    Instant updatedAt) {

  public static UserPrivacySettingsResponse from(User user) {
    return new UserPrivacySettingsResponse(
        user.getId(),
        user.isPrivateAccount(),
        user.isHideFollowerCount(),
        user.isHideFollowingCount(),
        user.isHideFollowerList(),
        user.isHideFollowingList(),
        user.isHideAddFriendButton(),
        user.isHideFollowButton(),
        user.isHideFromMutualList(),
        user.isMessageButtonEnabled(),
        user.getUpdatedAt());
  }
}
