package com.lambrk.dto;

public record UserPrivacySettingsRequest(
    Boolean privateAccount,
    Boolean hideFollowerCount,
    Boolean hideFollowingCount,
    Boolean hideFollowerList,
    Boolean hideFollowingList,
    Boolean hideAddFriendButton,
    Boolean hideFollowButton,
    Boolean hideFromMutualList,
    Boolean messageButtonEnabled
) {}
