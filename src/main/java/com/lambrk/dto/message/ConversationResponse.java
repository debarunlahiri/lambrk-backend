package com.lambrk.dto.message;

import com.lambrk.domain.message.ChatConversation;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A conversation entry in the inbox")
public record ConversationResponse(
    @Schema(description = "MongoDB document ID of the conversation") String id,
    @Schema(description = "Deterministic conversation identifier (sorted UUID pair)")
        String conversationId,
    @Schema(description = "The other participant's UUID") UUID otherParticipantId,
    @Schema(description = "The other participant's username") String otherParticipantUsername,
    @Schema(description = "Preview of the last message sent in this conversation")
        String lastMessage,
    @Schema(description = "UUID of who sent the last message") UUID lastMessageSenderId,
    @Schema(description = "When the last message was sent") Instant lastMessageAt,
    @Schema(description = "Number of unread messages for the requesting user") int unreadCount,
    @Schema(description = "When this conversation was first created") Instant createdAt) {
  public static ConversationResponse from(ChatConversation conv, UUID currentUserId) {
    UUID otherParticipantId =
        conv.getParticipantIds().stream()
            .filter(id -> !id.equals(currentUserId))
            .findFirst()
            .orElse(null);

    int participantIndex = conv.getParticipantIds().indexOf(otherParticipantId);
    String otherUsername =
        (participantIndex >= 0 && participantIndex < conv.getParticipantUsernames().size())
            ? conv.getParticipantUsernames().get(participantIndex)
            : null;

    return new ConversationResponse(
        conv.getId(),
        conv.getConversationId(),
        otherParticipantId,
        otherUsername,
        conv.getLastMessage(),
        conv.getLastMessageSenderId(),
        conv.getLastMessageAt(),
        conv.getUnreadCount(currentUserId),
        conv.getCreatedAt());
  }
}
