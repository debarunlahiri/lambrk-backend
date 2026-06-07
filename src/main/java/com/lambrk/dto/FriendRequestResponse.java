package com.lambrk.dto;

import com.lambrk.domain.UserFriendship;

import java.time.Instant;
import java.util.UUID;

public record FriendRequestResponse(
    UUID id,
    UserResponse requester,
    UserResponse addressee,
    String status,
    String source,
    String requestMessage,
    Instant createdAt,
    Instant respondedAt
) {

    public static FriendRequestResponse from(UserFriendship friendship) {
        return new FriendRequestResponse(
            friendship.getId(),
            UserResponse.from(friendship.getRequester()),
            UserResponse.from(friendship.getAddressee()),
            friendship.getStatus().name(),
            friendship.getSource(),
            friendship.getRequestMessage(),
            friendship.getCreatedAt(),
            friendship.getRespondedAt()
        );
    }
}
